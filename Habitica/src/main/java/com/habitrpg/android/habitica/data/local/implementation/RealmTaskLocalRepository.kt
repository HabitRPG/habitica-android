package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.TaskLocalRepository
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.RemindersItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.shared.habitica.models.responses.TaskDirectionData
import com.habitrpg.shared.habitica.models.tasks.TaskType
import com.habitrpg.shared.habitica.models.tasks.TasksOrder
import io.realm.Realm
import io.realm.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.Locale

class RealmTaskLocalRepository(
    realm: Realm,
) : RealmBaseLocalRepository(realm),
    TaskLocalRepository {
    override fun getTasks(
        taskType: TaskType,
        userID: String,
        includedGroupIDs: Array<String>,
    ): Flow<List<Task>> = safeFindAll {
        it.where(Task::class.java)
            .equalTo("typeValue", taskType.value)
            .equalTo("ownerID", userID)
            .sort("position", Sort.ASCENDING, "dateCreated", Sort.DESCENDING)
    }

    override fun getTasksWithTaskId(taskId: String): List<Task> =
        safeQuery { it.where(Task::class.java).equalTo("id", taskId) }?.findAll() ?: emptyList()

    override fun getTasks(userId: String): Flow<List<Task>> = safeFindAll {
        it.where(Task::class.java)
            .equalTo("ownerID", userId)
            .sort("position", Sort.ASCENDING, "dateCreated", Sort.DESCENDING)
    }

    override fun saveTasks(
        ownerID: String,
        tasksOrder: TasksOrder,
        tasks: TaskList,
    ) {
        val sortedTasks = mutableListOf<Task>()
        sortedTasks.addAll(sortTasks(tasks.tasks, tasksOrder.habits))
        sortedTasks.addAll(sortTasks(tasks.tasks, tasksOrder.dailys))
        sortedTasks.addAll(sortTasks(tasks.tasks, tasksOrder.todos))
        sortedTasks.addAll(sortTasks(tasks.tasks, tasksOrder.rewards))
        for (task in tasks.tasks.values) {
            task.position = (sortedTasks.lastOrNull { it.type == task.type }?.position ?: -1) + 1
            sortedTasks.add(task)
        }
        removeOldTasks(ownerID, sortedTasks)

        val allChecklistItems = ArrayList<ChecklistItem>()
        val allReminders = ArrayList<RemindersItem>()
        sortedTasks.forEach {
            if (it.ownerID.isBlank()) {
                it.ownerID = ownerID
            }
            it.checklist?.let { it1 -> allChecklistItems.addAll(it1) }
            it.reminders?.let { it1 -> allReminders.addAll(it1) }
        }
        removeOldReminders(allReminders)
        removeOldChecklists(allChecklistItems)

        executeTransaction { realm1 -> realm1.insertOrUpdate(sortedTasks) }
    }

    override fun handleTaskResponse(
        user: User,
        res: TaskDirectionData,
        task: Task,
        up: Boolean,
        localDelta: Float,
    ) {
        executeTransaction { realm ->
            val bgTask = getLiveObject(task) ?: return@executeTransaction
            val bgUser = getLiveObject(user) ?: return@executeTransaction
            if (bgTask.type != TaskType.REWARD && (bgTask.value - localDelta) + res.delta != bgTask.value) {
                bgTask.value = (bgTask.value - localDelta) + res.delta
                if (TaskType.DAILY == bgTask.type) {
                    if (up) {
                        bgTask.streak = (bgTask.streak ?: 0) + 1
                    } else {
                        bgTask.streak = (bgTask.streak ?: 0) - 1
                    }
                } else if (TaskType.HABIT == bgTask.type) {
                    if (up) {
                        bgTask.counterUp = (bgTask.counterUp ?: 0) + 1
                    } else {
                        bgTask.counterDown = (bgTask.counterDown ?: 0) + 1
                    }
                }
            }

            if (TaskType.DAILY == bgTask.type || TaskType.TODO == bgTask.type) {
                bgTask.completeForUser(user.id, up)
                if (bgTask.isGroupTask) {
                    val entry =
                        bgTask.group?.assignedUsersDetail?.firstOrNull { it.assignedUserID == user.id }
                    entry?.completed = up
                    if (up) {
                        entry?.completedDate = Date()
                    } else {
                        entry?.completedDate = null
                    }
                }
            }

            val taskId = bgTask.id
            if (taskId != null) {
                getTasksWithTaskId(taskId).forEach { sibling ->
                    if (sibling.ownerID != bgTask.ownerID) {
                        sibling.value = bgTask.value
                        sibling.streak = bgTask.streak
                        sibling.completed = bgTask.completed
                        sibling.counterUp = bgTask.counterUp
                        sibling.counterDown = bgTask.counterDown
                        if (sibling.isGroupTask) {
                            sibling.group
                                ?.assignedUsersDetail
                                ?.firstOrNull { detail -> detail.assignedUserID == user.id }
                                ?.let { detail ->
                                    detail.completed = up
                                    detail.completedDate = if (up) Date() else null
                                }
                        }
                    }
                }
            }
            res._tmp?.drop?.key?.let { key ->
                val type =
                    when (
                        res._tmp
                            ?.drop
                            ?.type
                            ?.lowercase(Locale.US)
                    ) {
                        "hatchingpotion" -> {
                            "hatchingPotions"
                        }

                        "egg" -> {
                            "eggs"
                        }

                        else -> {
                            res._tmp
                                ?.drop
                                ?.type
                                ?.lowercase(Locale.US)
                        }
                    }
                var item =
                    safeQuery {
                        it.where(OwnedItem::class.java)
                            .equalTo("itemType", type)
                            .equalTo("key", key)
                    }?.findFirst()
                if (item == null) {
                    item = OwnedItem()
                    item.key = key
                    item.itemType = type
                    item.userID = user.id

                    when (type) {
                        "eggs" -> bgUser.items?.eggs?.add(item)
                        "food" -> bgUser.items?.food?.add(item)
                        "hatchingPotions" -> bgUser.items?.hatchingPotions?.add(item)
                        "quests" -> bgUser.items?.quests?.add(item)
                    }
                }
                item.numberOwned += 1
            }

            bgUser.stats?.hp = res.hp
            bgUser.stats?.exp = res.exp
            bgUser.stats?.mp = res.mp
            bgUser.stats?.gp = res.gp
            bgUser.stats?.lvl = res.lvl
            bgUser.party
                ?.quest
                ?.progress
                ?.up = (
                    bgUser.party
                        ?.quest
                        ?.progress
                        ?.up
                        ?: 0F
                    ) + (
                    res._tmp
                        ?.quest
                        ?.progressDelta
                        ?.toFloat() ?: 0F
                    )
        }
    }

    override fun saveCompletedTodos(
        userId: String,
        tasks: MutableCollection<Task>,
    ) {
        removeCompletedTodos(userId, tasks)
        executeTransaction { realm1 -> realm1.insertOrUpdate(tasks) }
    }

    private fun removeOldChecklists(onlineItems: List<ChecklistItem>) {
        val localItems =
            safeQuery { it.where(ChecklistItem::class.java) }?.findAll()?.createSnapshot() ?: return
        val itemsToDelete = localItems.filterNot { onlineItems.contains(it) }
        realm.executeTransaction {
            for (item in itemsToDelete) {
                item.deleteFromRealm()
            }
        }
    }

    private fun removeOldReminders(onlineReminders: List<RemindersItem>) {
        val localReminders =
            safeQuery { it.where(RemindersItem::class.java) }?.findAll()?.createSnapshot() ?: return
        val itemsToDelete = localReminders.filterNot { onlineReminders.contains(it) }
        realm.executeTransaction {
            for (item in itemsToDelete) {
                item.deleteFromRealm()
            }
        }
    }

    private fun sortTasks(
        taskMap: MutableMap<String, Task>,
        taskOrder: List<String>,
    ): List<Task> {
        val taskList = ArrayList<Task>()
        var position = 0
        for (taskId in taskOrder) {
            val task = taskMap[taskId]
            if (task != null) {
                task.position = position
                taskList.add(task)
                position++
                taskMap.remove(taskId)
            }
        }
        return taskList
    }

    private fun removeOldTasks(
        ownerID: String,
        onlineTaskList: List<Task>,
    ) {
        val localTasks =
            safeQuery {
                it.where(Task::class.java)
                    .equalTo("ownerID", ownerID)
                    .beginGroup()
                    .beginGroup()
                    .equalTo("typeValue", TaskType.TODO.value)
                    .equalTo("completed", false)
                    .endGroup()
                    .or()
                    .notEqualTo("typeValue", TaskType.TODO.value)
                    .endGroup()
            }?.findAll()?.createSnapshot() ?: return
        val tasksToDelete = localTasks.filterNot { onlineTaskList.contains(it) }
        executeTransaction {
            for (localTask in tasksToDelete) {
                localTask.deleteFromRealm()
            }
        }
    }

    private fun removeCompletedTodos(
        userID: String,
        onlineTaskList: MutableCollection<Task>,
    ) {
        val localTasks =
            safeQuery {
                it.where(Task::class.java)
                    .equalTo("ownerID", userID)
                    .equalTo("typeValue", TaskType.TODO.value)
                    .equalTo("completed", true)
            }?.findAll()?.createSnapshot() ?: return
        val tasksToDelete = localTasks.filterNot { onlineTaskList.contains(it) }
        executeTransaction {
            for (localTask in tasksToDelete) {
                localTask.deleteFromRealm()
            }
        }
    }

    override fun deleteTask(taskID: String) {
        val task = safeQuery { it.where(Task::class.java).equalTo("id", taskID) }?.findFirst()
        executeTransaction {
            if (task?.isManaged == true) {
                task.deleteFromRealm()
            }
        }
    }

    override fun getTask(taskId: String): Flow<Task> = safeFindOne {
        it.where(Task::class.java).equalTo("id", taskId)
    }

    override fun getTaskCopy(taskId: String): Flow<Task> {
        return getTask(taskId)
            .map { task ->
                return@map if (task.isManaged && task.isValid) {
                    realm.copyFromRealm(task)
                } else {
                    task
                }
            }
    }

    override fun markTaskCompleted(
        taskId: String,
        isCompleted: Boolean,
    ) {
        val task = safeQuery { it.where(Task::class.java).equalTo("id", taskId) }?.findFirst()
        executeTransaction { task?.completed = isCompleted }
    }

    override fun swapTaskPosition(
        firstPosition: Int,
        secondPosition: Int,
    ) {
        val firstTask =
            safeQuery { it.where(Task::class.java).equalTo("position", firstPosition) }?.findFirst()
        val secondTask =
            safeQuery { it.where(Task::class.java).equalTo("position", secondPosition) }?.findFirst()
        if (firstTask != null && secondTask != null && firstTask.isValid && secondTask.isValid) {
            executeTransaction {
                firstTask.position = secondPosition
                secondTask.position = firstPosition
            }
        }
    }

    override fun getTaskAtPosition(
        taskType: String,
        position: Int,
    ): Flow<Task> = safeFindOne {
        it.where(Task::class.java)
            .equalTo("typeValue", taskType)
            .equalTo("position", position)
    }

    override fun updateIsdue(daily: TaskList): TaskList {
        val tasks =
            safeQuery { it.where(Task::class.java).equalTo("typeValue", TaskType.DAILY.value) }
                ?.findAll() ?: return daily
        realm.beginTransaction()
        tasks
            .filter { daily.tasks.containsKey(it.id) }
            .forEach { it.isDue = daily.tasks[it.id]?.isDue }
        realm.commitTransaction()
        return daily
    }

    override fun updateTaskPositions(taskOrder: List<String>) {
        if (taskOrder.isEmpty()) return
        val tasks =
            safeQuery { it.where(Task::class.java).`in`("id", taskOrder.toTypedArray()) }
                ?.findAll() ?: return
        executeTransaction { _ ->
            tasks
                .filter { taskOrder.contains(it.id) }
                .forEach { it.position = taskOrder.indexOf(it.id) }
        }
    }

    override fun getErroredTasks(userID: String): Flow<List<Task>> = safeFindAll {
        it.where(Task::class.java)
            .equalTo("ownerID", userID)
            .equalTo("hasErrored", true)
            .sort("position")
    }

    override fun getUser(userID: String): Flow<User> = queryUser(userID)

    override fun getTasksForChallenge(
        challengeID: String?,
        userID: String?,
    ): Flow<List<Task>> = safeFindAll {
        it.where(Task::class.java)
            .equalTo("challengeID", challengeID)
            .equalTo("ownerID", userID)
    }
}
