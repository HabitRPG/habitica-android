package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.TaskLocalRepository
import com.habitrpg.android.habitica.models.tasks.*
import com.habitrpg.shared.habitica.models.user.User
import com.habitrpg.shared.habitica.models.tasks.ChecklistItem
import com.habitrpg.shared.habitica.models.tasks.RemindersItem
import com.habitrpg.shared.habitica.models.tasks.TaskList
import com.habitrpg.shared.habitica.models.tasks.TasksOrder
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.Sort

class RealmTaskLocalRepository(realm: Realm) : RealmBaseLocalRepository(realm), TaskLocalRepository {

    override fun getTasks(taskType: String, userID: String): Flowable<RealmResults<Task>> {
        if (realm.isClosed) {
            return Flowable.empty()
        }
        return realm.where(Task::class.java)
                .equalTo("type", taskType)
                .equalTo("userId", userID)
                .sort("position", Sort.ASCENDING, "dateCreated", Sort.DESCENDING)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
                .retry(1)
    }

    override fun getTasks(userId: String): Flowable<RealmResults<Task>> {
        if (realm.isClosed) {
            return Flowable.empty()
        }
        return realm.where(Task::class.java).equalTo("userId", userId)
                .sort("position", Sort.ASCENDING, "dateCreated", Sort.DESCENDING)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun saveTasks(userId: String, tasksOrder: TasksOrder, tasks: TaskList) {
        val sortedTasks = ArrayList<Task>()
        sortedTasks.addAll(sortTasks(tasks.tasks, tasksOrder.habits))
        sortedTasks.addAll(sortTasks(tasks.tasks, tasksOrder.dailys))
        sortedTasks.addAll(sortTasks(tasks.tasks, tasksOrder.todos))
        sortedTasks.addAll(sortTasks(tasks.tasks, tasksOrder.rewards))
        removeOldTasks(userId, sortedTasks)

        val allChecklistItems = ArrayList<ChecklistItem>()
        sortedTasks.forEach { it.checklist?.let { it1 -> allChecklistItems.addAll(it1) } }
        removeOldChecklists(allChecklistItems)

        val allReminders = ArrayList<RemindersItem>()
        sortedTasks.forEach { it.reminders?.let { it1 -> allReminders.addAll(it1) } }
        removeOldReminders(allReminders)

        realm.executeTransactionAsync { realm1 -> realm1.insertOrUpdate(sortedTasks) }
    }

    override fun saveCompletedTodos(userId: String, tasks: MutableCollection<Task>) {
        removeCompletedTodos(userId, tasks)
        realm.executeTransactionAsync { realm1 -> realm1.insertOrUpdate(tasks) }
    }

    private fun sortTasks(taskMap: MutableMap<String, Task>, taskOrder: List<String>): List<Task> {
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
        for (task in taskMap.values) {
            task.position = position
            taskList.add(task)
            position++
        }
        return taskList
    }

    private fun removeOldTasks(userID: String, onlineTaskList: List<Task>) {
        if (realm.isClosed) return
        val localTasks = realm.where(Task::class.java)
                .equalTo("userId", userID)
                .beginGroup()
                .beginGroup()
                .equalTo("type", TaskType.TYPE_TODO)
                .equalTo("completed", false)
                .endGroup()
                .or()
                .notEqualTo("type", TaskType.TYPE_TODO)
                .endGroup()
                .findAll()
                .createSnapshot()
        val tasksToDelete = localTasks.filterNot { onlineTaskList.contains(it) }
        executeTransaction {
            for (localTask in tasksToDelete) {
                localTask.checklist?.deleteAllFromRealm()
                localTask.reminders?.deleteAllFromRealm()
                localTask.deleteFromRealm()
            }
        }
    }

    private fun removeCompletedTodos(userID: String, onlineTaskList: MutableCollection<Task>) {
        val localTasks = realm.where(Task::class.java)
                .equalTo("userId", userID)
                .equalTo("type", TaskType.TYPE_TODO)
                .equalTo("completed", true)
                .findAll()
                .createSnapshot()
        val tasksToDelete = localTasks.filterNot { onlineTaskList.contains(it) }
        executeTransaction {
            for (localTask in tasksToDelete) {
                localTask.checklist?.deleteAllFromRealm()
                localTask.reminders?.deleteAllFromRealm()
                localTask.deleteFromRealm()
            }
        }
    }

    private fun removeOldChecklists(onlineItems: List<ChecklistItem>) {
        val localItems = realm.where(ChecklistItem::class.java).findAll().createSnapshot()
        val itemsToDelete = localItems.filterNot { onlineItems.contains(it) }
        executeTransaction {
            for (item in itemsToDelete) {
                item.deleteFromRealm()
            }
        }
    }

    private fun removeOldReminders(onlineReminders: List<RemindersItem>) {
        val localReminders = realm.where(RemindersItem::class.java).findAll().createSnapshot()
        val itemsToDelete = localReminders.filterNot { onlineReminders.contains(it) }
        executeTransaction {
            for (item in itemsToDelete) {
                item.deleteFromRealm()
            }
        }
    }

    override fun deleteTask(taskID: String) {
        val task = realm.where(Task::class.java).equalTo("id", taskID).findFirstAsync()
        executeTransaction {
            if (task.isManaged) {
                task.deleteFromRealm()
            }
        }
    }

    override fun getTask(taskId: String): Flowable<Task> {
        if (realm.isClosed) {
            return Flowable.empty()
        }
        return realm.where(Task::class.java).equalTo("id", taskId).findFirstAsync().asFlowable<RealmObject>()
                .filter { realmObject -> realmObject.isLoaded }
                .cast(Task::class.java)
    }

    override fun getTaskCopy(taskId: String): Flowable<Task> {
        return getTask(taskId)
                .map { task ->
                    return@map if (task.isManaged && task.isValid) {
                         realm.copyFromRealm(task)
                    } else {
                         task
                    }
                }
    }

    override fun markTaskCompleted(taskId: String, isCompleted: Boolean) {
        val task = realm.where(Task::class.java).equalTo("id", taskId).findFirstAsync()
        executeTransaction { task.completed = true }
    }

    override fun saveReminder(remindersItem: RemindersItem) {
        executeTransaction { it.insertOrUpdate(remindersItem) }
    }

    override fun swapTaskPosition(firstPosition: Int, secondPosition: Int) {
        val firstTask = realm.where(Task::class.java).equalTo("position", firstPosition).findFirst()
        val secondTask = realm.where(Task::class.java).equalTo("position", secondPosition).findFirst()
        if (firstTask != null && secondTask != null && firstTask.isValid && secondTask.isValid) {
            executeTransaction {
                firstTask.position = secondPosition
                secondTask.position = firstPosition
            }
        }
    }

    override fun getTaskAtPosition(taskType: String, position: Int): Flowable<Task> {
        return realm.where(Task::class.java).equalTo("type", taskType).equalTo("position", position).findFirstAsync().asFlowable<RealmObject>()
                .filter { realmObject -> realmObject.isLoaded }
                .cast(Task::class.java)
    }

    override fun updateIsdue(daily: TaskList): Maybe<TaskList> {
        return Flowable.just(realm.where(Task::class.java).equalTo("type", "daily").findAll())
                .firstElement()
                .map { tasks ->
                    realm.beginTransaction()
                    tasks.filter { daily.tasks.containsKey(it.id) }.forEach { it.isDue = daily.tasks[it.id]?.isDue }
                    realm.commitTransaction()
                    daily
                }
    }

    override fun updateTaskPositions(taskOrder: List<String>) {
        if (taskOrder.isNotEmpty()) {
            val tasks = realm.where(Task::class.java).`in`("id", taskOrder.toTypedArray()).findAll()
            executeTransaction { _ ->
                tasks.filter { taskOrder.contains(it.id) }.forEach { it.position = taskOrder.indexOf(it.id) }
            }
        }
    }

    override fun getErroredTasks(userID: String): Flowable<RealmResults<Task>> {
        return realm.where(Task::class.java)
                .equalTo("userId", userID)
                .equalTo("hasErrored", true)
                .sort("position")
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
                .retry(1)    }

    override fun getUser(userID: String): Flowable<User> {
        return realm.where(User::class.java)
                .equalTo("id", userID)
                .findAll()
                .asFlowable()
                .filter { realmObject -> realmObject.isLoaded && realmObject.isValid && !realmObject.isEmpty() }
                .map { users -> users.first() }
    }
}
