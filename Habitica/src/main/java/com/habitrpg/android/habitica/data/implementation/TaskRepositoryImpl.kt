package com.habitrpg.android.habitica.data.implementation

import androidx.core.os.bundleOf
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.local.TaskLocalRepository
import com.habitrpg.android.habitica.helpers.Analytics
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.interactors.ScoreTaskLocallyInteractor
import com.habitrpg.android.habitica.models.BaseMainObject
import com.habitrpg.android.habitica.models.responses.BulkTaskScoringData
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import com.habitrpg.common.habitica.helpers.AnalyticsManager
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.shared.habitica.models.responses.TaskDirection
import com.habitrpg.shared.habitica.models.responses.TaskDirectionData
import com.habitrpg.shared.habitica.models.responses.TaskScoringResult
import com.habitrpg.shared.habitica.models.tasks.TaskType
import com.habitrpg.shared.habitica.models.tasks.TasksOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@ExperimentalCoroutinesApi
class TaskRepositoryImpl(
    localRepository: TaskLocalRepository,
    apiClient: ApiClient,
    authenticationHandler: AuthenticationHandler,
    val appConfigManager: AppConfigManager,
    val analyticsManager: AnalyticsManager
) : BaseRepositoryImpl<TaskLocalRepository>(localRepository, apiClient, authenticationHandler), TaskRepository {
    private var lastTaskAction: Long = 0

    override fun getTasks(taskType: TaskType, userID: String?, includedGroupIDs: Array<String>): Flow<List<Task>> =
        this.localRepository.getTasks(taskType, userID ?: authenticationHandler.currentUserID ?: "", includedGroupIDs)

    override fun saveTasks(userId: String, order: TasksOrder, tasks: TaskList) {
        localRepository.saveTasks(userId, order, tasks)
    }

    override suspend fun retrieveTasks(userId: String, tasksOrder: TasksOrder): TaskList? {
        val tasks = apiClient.getTasks() ?: return null
        this.localRepository.saveTasks(userId, tasksOrder, tasks)
        return tasks
    }

    override suspend fun retrieveCompletedTodos(userId: String?): TaskList? {
        val taskList = this.apiClient.getTasks("completedTodos") ?: return null
        val tasks = taskList.tasks
        this.localRepository.saveCompletedTodos(userId ?: authenticationHandler.currentUserID ?: "", tasks.values)
        return taskList
    }

    override suspend fun retrieveTasks(userId: String, tasksOrder: TasksOrder, dueDate: Date): TaskList? {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.US)
        val taskList = this.apiClient.getTasks("dailys", formatter.format(dueDate)) ?: return null
        this.localRepository.saveTasks(userId, tasksOrder, taskList)
        return taskList
    }

    @Suppress("ReturnCount")
    override suspend fun taskChecked(
        user: User?,
        task: Task,
        up: Boolean,
        force: Boolean,
        notifyFunc: ((TaskScoringResult) -> Unit)?
    ): TaskScoringResult? {
        val localData = if (user != null && appConfigManager.enableLocalTaskScoring()) {
            ScoreTaskLocallyInteractor.score(user, task, if (up) TaskDirection.UP else TaskDirection.DOWN)
        } else {
            null
        }
        if (user != null && localData != null) {
            val stats = user.stats
            val result = TaskScoringResult(localData, stats)
            notifyFunc?.invoke(result)

            handleTaskResponse(user, localData, task, up, 0f)
        }
        val now = Date().time
        val id = task.id
        if (lastTaskAction > now - 500 && !force || id == null) {
            return null
        }

        lastTaskAction = now
        val res = this.apiClient.postTaskDirection(id, (if (up) TaskDirection.UP else TaskDirection.DOWN).text) ?: return null
        // There are cases where the user object is not set correctly. So the app refetches it as a fallback
        val thisUser = user ?: localRepository.getUser(authenticationHandler.currentUserID ?: "").firstOrNull() ?: return null
        // save local task changes

        Analytics.logEvent(
            "task_scored",
            bundleOf(
                Pair("type", task.type),
                Pair("scored_up", up),
                Pair("value", task.value)
            )
        )
        if (res.lvl == 0) {
            // Team tasks that require approval have weird data that we should just ignore.
            return TaskScoringResult()
        }
        val result = TaskScoringResult(res, thisUser.stats)
        if (localData == null) {
            notifyFunc?.invoke(result)
        }
        handleTaskResponse(thisUser, res, task, up, localData?.delta ?: 0f)
        return result
    }

    override suspend fun bulkScoreTasks(data: List<Map<String, String>>): BulkTaskScoringData? {
        return apiClient.bulkScoreTasks(data)
    }

    private fun handleTaskResponse(
        user: User,
        res: TaskDirectionData,
        task: Task,
        up: Boolean,
        localDelta: Float
    ) {
        this.localRepository.executeTransaction {
            val bgTask = localRepository.getLiveObject(task) ?: return@executeTransaction
            val bgUser = localRepository.getLiveObject(user) ?: return@executeTransaction
            if (bgTask.type != TaskType.REWARD && (bgTask.value - localDelta) + res.delta != bgTask.value) {
                bgTask.value = (bgTask.value - localDelta) + res.delta
                if (TaskType.DAILY == bgTask.type || TaskType.TODO == bgTask.type) {
                    bgTask.completeForUser(authenticationHandler.currentUserID ?: "", up)
                    if (TaskType.DAILY == bgTask.type) {
                        if (up) {
                            bgTask.streak = (bgTask.streak ?: 0) + 1
                        } else {
                            bgTask.streak = (bgTask.streak ?: 0) - 1
                        }
                    }
                } else if (TaskType.HABIT == bgTask.type) {
                    if (up) {
                        bgTask.counterUp = (bgTask.counterUp ?: 0) + 1
                    } else {
                        bgTask.counterDown = (bgTask.counterDown ?: 0) + 1
                    }
                }

                if (bgTask.isGroupTask) {
                    val entry = bgTask.group?.assignedUsersDetail?.firstOrNull { it.assignedUserID == user.id }
                    entry?.completed = up
                    if (up) {
                        entry?.completedDate = Date()
                    } else {
                        entry?.completedDate = null
                    }
                }
            }
            res._tmp?.drop?.key?.let { key ->
                val type = when (res._tmp?.drop?.type?.lowercase(Locale.US)) {
                    "hatchingpotion" -> "hatchingPotions"
                    "egg" -> "eggs"
                    else -> res._tmp?.drop?.type?.lowercase(Locale.US)
                }
                var item = it.where(OwnedItem::class.java).equalTo("itemType", type).equalTo("key", key).findFirst()
                if (item == null) {
                    item = OwnedItem()
                    item.key = key
                    item.itemType = type
                    item.userID = user.id
                }
                item.numberOwned += 1
                when (type) {
                    "eggs" -> bgUser.items?.eggs?.add(item)
                    "food" -> bgUser.items?.food?.add(item)
                    "hatchingPotions" -> bgUser.items?.hatchingPotions?.add(item)
                    "quests" -> bgUser.items?.quests?.add(item)
                    else -> ""
                }
            }

            bgUser.stats?.hp = res.hp
            bgUser.stats?.exp = res.exp
            bgUser.stats?.mp = res.mp
            bgUser.stats?.gp = res.gp
            bgUser.stats?.lvl = res.lvl
            bgUser.party?.quest?.progress?.up = (
                bgUser.party?.quest?.progress?.up
                    ?: 0F
                ) + (res._tmp?.quest?.progressDelta?.toFloat() ?: 0F)
        }
    }

    override suspend fun markTaskNeedsWork(task: Task, userID: String) {
        val savedTask = apiClient.markTaskNeedsWork(task.id ?: "", userID)
        if (savedTask != null) {
            savedTask.id = task.id
            savedTask.position = task.position
            savedTask.group?.assignedUsersDetail?.firstOrNull { it.assignedUserID == userID }?.let {
                it.completed = false
                it.completedDate = null
            }
            localRepository.save(savedTask)
        }
    }

    override suspend fun taskChecked(
        user: User?,
        taskId: String,
        up: Boolean,
        force: Boolean,
        notifyFunc: ((TaskScoringResult) -> Unit)?
    ): TaskScoringResult? {
        val task = localRepository.getTask(taskId).firstOrNull() ?: return null
        return taskChecked(user, task, up, force, notifyFunc)
    }

    override suspend fun scoreChecklistItem(taskId: String, itemId: String): Task? {
        val task = apiClient.scoreChecklistItem(taskId, itemId)
        val updatedItem: ChecklistItem? = task?.checklist?.lastOrNull { itemId == it.id }
        if (updatedItem != null) {
            localRepository.save(updatedItem)
        }
        return task
    }

    override fun getTask(taskId: String) = localRepository.getTask(taskId)

    override fun getTaskCopy(taskId: String) = localRepository.getTaskCopy(taskId)

    override suspend fun createTask(task: Task, force: Boolean): Task? {
        val now = Date().time
        if (lastTaskAction > now - 500 && !force) {
            return null
        }
        lastTaskAction = now

        task.isSaving = true
        task.isCreating = true
        task.hasErrored = false
        task.ownerID = if (task.isGroupTask) {
            task.group?.groupID ?: ""
        } else {
            authenticationHandler.currentUserID ?: ""
        }
        if (task.id == null) {
            task.id = UUID.randomUUID().toString()
        }
        localRepository.save(task)

        val savedTask = if (task.isGroupTask) {
            apiClient.createGroupTask(task.group?.groupID ?: "", task)
        } else {
            apiClient.createTask(task)
        }
        savedTask?.dateCreated = Date()
        if (savedTask != null) {
            savedTask.tags = task.tags
            localRepository.save(savedTask)
        } else {
            task.hasErrored = true
            task.isSaving = false
            localRepository.save(task)
        }
        return savedTask
    }

    @Suppress("ReturnCount")
    override suspend fun updateTask(task: Task, force: Boolean): Task? {
        val now = Date().time
        if ((lastTaskAction > now - 500 && !force) || !task.isValid) {
            return task
        }
        lastTaskAction = now
        val id = task.id ?: return task
        val unmanagedTask = localRepository.getUnmanagedCopy(task)
        unmanagedTask.isSaving = true
        unmanagedTask.hasErrored = false
        localRepository.save(unmanagedTask)
        val savedTask = apiClient.updateTask(id, unmanagedTask)
        savedTask?.position = task.position
        savedTask?.id = task.id
        savedTask?.ownerID = task.ownerID
        if (savedTask != null) {
            savedTask.tags = task.tags
            localRepository.save(savedTask)
        } else {
            unmanagedTask.hasErrored = true
            unmanagedTask.isSaving = false
            localRepository.save(unmanagedTask)
        }
        return savedTask
    }

    override suspend fun deleteTask(taskId: String): Void? {
        apiClient.deleteTask(taskId) ?: return null
        localRepository.deleteTask(taskId)
        return null
    }

    override fun saveTask(task: Task) {
        localRepository.save(task)
    }

    override suspend fun createTasks(newTasks: List<Task>) = apiClient.createTasks(newTasks)

    override fun markTaskCompleted(taskId: String, isCompleted: Boolean) {
        localRepository.markTaskCompleted(taskId, isCompleted)
    }

    override fun <T : BaseMainObject> modify(obj: T, transaction: (T) -> Unit) {
        localRepository.modify(obj, transaction)
    }

    override fun swapTaskPosition(firstPosition: Int, secondPosition: Int) {
        localRepository.swapTaskPosition(firstPosition, secondPosition)
    }

    override suspend fun updateTaskPosition(taskType: TaskType, taskID: String, newPosition: Int): List<String>? {
        val positions = apiClient.postTaskNewPosition(taskID, newPosition) ?: return null
        localRepository.updateTaskPositions(positions)
        return positions
    }

    override fun getUnmanagedTask(taskid: String) = getTask(taskid).map { localRepository.getUnmanagedCopy(it) }

    override fun updateTaskInBackground(task: Task, assignChanges: Map<String, MutableList<String>>) {
        MainScope().launchCatching {
            val updatedTask = updateTask(task) ?: return@launchCatching
            handleAssignmentChanges(updatedTask, assignChanges)
        }
    }

    override fun createTaskInBackground(task: Task, assignChanges: Map<String, MutableList<String>>) {
        MainScope().launchCatching {
            val createdTask = createTask(task) ?: return@launchCatching
            handleAssignmentChanges(createdTask, assignChanges)
        }
    }

    private suspend fun handleAssignmentChanges(task: Task, assignChanges: Map<String, MutableList<String>>) {
        val taskID = task.id ?: return
        assignChanges["assign"]?.let { assignments ->
            if (assignments.isEmpty()) return@let
            val savedTask = apiClient.assignToTask(taskID, assignments) ?: return@let
            savedTask.id = task.id
            savedTask.ownerID = task.ownerID
            savedTask.position = task.position
            localRepository.save(savedTask)
        }

        assignChanges["unassign"]?.let { unassignments ->
            var savedTask: Task? = null
            for (unassignment in unassignments) {
                savedTask = apiClient.unassignFromTask(taskID, unassignment)
            }
            if (savedTask != null) {
                savedTask.id = task.id
                savedTask.position = task.position
                savedTask.ownerID = task.ownerID
                localRepository.save(savedTask)
            }
        }
    }

    override fun getTaskCopies(): Flow<List<Task>> = authenticationHandler.userIDFlow.flatMapLatest {
        localRepository.getTasks(it)
    }.map { localRepository.getUnmanagedCopy(it) }

    override fun getTaskCopies(tasks: List<Task>): List<Task> = localRepository.getUnmanagedCopy(tasks)

    override suspend fun retrieveDailiesFromDate(date: Date): TaskList? {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.US)
        return apiClient.getTasks("dailys", formatter.format(date))
    }

    override suspend fun syncErroredTasks(): List<Task>? {
        val tasks = localRepository.getErroredTasks(currentUserID ?: "").firstOrNull()
        return tasks?.map { localRepository.getUnmanagedCopy(it) }?.mapNotNull {
            if (it.isCreating) {
                createTask(it, true)
            } else {
                updateTask(it, true)
            }
        }
    }

    override suspend fun unlinkAllTasks(challengeID: String?, keepOption: String): Void? {
        return apiClient.unlinkAllTasks(challengeID, keepOption)
    }

    override fun getTasksForChallenge(challengeID: String?): Flow<List<Task>> {
        return localRepository.getTasksForChallenge(challengeID, currentUserID ?: "")
    }
}
