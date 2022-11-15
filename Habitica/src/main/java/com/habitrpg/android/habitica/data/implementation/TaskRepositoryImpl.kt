package com.habitrpg.android.habitica.data.implementation

import androidx.core.os.bundleOf
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.local.TaskLocalRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.ExceptionHandler
import com.habitrpg.android.habitica.interactors.ScoreTaskLocallyInteractor
import com.habitrpg.android.habitica.models.BaseMainObject
import com.habitrpg.android.habitica.models.responses.BulkTaskScoringData
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.proxy.AnalyticsManager
import com.habitrpg.shared.habitica.models.responses.TaskDirection
import com.habitrpg.shared.habitica.models.responses.TaskDirectionData
import com.habitrpg.shared.habitica.models.responses.TaskScoringResult
import com.habitrpg.shared.habitica.models.tasks.TaskType
import com.habitrpg.shared.habitica.models.tasks.TasksOrder
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class TaskRepositoryImpl(
    localRepository: TaskLocalRepository,
    apiClient: ApiClient,
    userID: String,
    val appConfigManager: AppConfigManager,
    val analyticsManager: AnalyticsManager
) : BaseRepositoryImpl<TaskLocalRepository>(localRepository, apiClient, userID), TaskRepository {
    private var lastTaskAction: Long = 0

    override fun getTasks(taskType: TaskType, userID: String?, includedGroupIDs: Array<String>): Flow<List<Task>> =
        this.localRepository.getTasks(taskType, userID ?: this.userID, includedGroupIDs)

    override fun getTasksFlowable(taskType: TaskType, userID: String?, includedGroupIDs: Array<String>): Flowable<out List<Task>> =
        this.localRepository.getTasksFlowable(taskType, userID ?: this.userID, includedGroupIDs)

    override fun saveTasks(userId: String, order: TasksOrder, tasks: TaskList) {
        localRepository.saveTasks(userId, order, tasks)
    }

    override suspend fun retrieveTasks(userId: String, tasksOrder: TasksOrder): TaskList? {
        val tasks = apiClient.getTasks() ?: return null
        this.localRepository.saveTasks(userId, tasksOrder, tasks)
        return tasks
    }

    override fun retrieveCompletedTodos(userId: String?): Flowable<TaskList> {
        return this.apiClient.getTasks("completedTodos")
            .doOnNext { taskList ->
                val tasks = taskList.tasks
                this.localRepository.saveCompletedTodos(userId ?: this.userID, tasks.values)
            }
    }

    override fun retrieveTasks(userId: String, tasksOrder: TasksOrder, dueDate: Date): Flowable<TaskList> {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.US)
        return this.apiClient.getTasks("dailys", formatter.format(dueDate))
            .doOnNext { res -> this.localRepository.saveTasks(userId, tasksOrder, res) }
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
        } else null
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
        val thisUser = user ?: localRepository.getUser(userID).firstOrNull() ?: return null
        // save local task changes

        analyticsManager.logEvent(
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

    override fun bulkScoreTasks(data: List<Map<String, String>>): Flowable<BulkTaskScoringData> {
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
                    bgTask.completeForUser(userID, up)
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
            localRepository.modify(updatedItem) { liveItem -> liveItem.completed = !liveItem.completed }
        }
        return task
    }

    override fun getTask(taskId: String) = localRepository.getTask(taskId)

    override fun getTaskCopy(taskId: String) = localRepository.getTaskCopy(taskId)

    override fun createTask(task: Task, force: Boolean): Flowable<Task> {
        val now = Date().time
        if (lastTaskAction > now - 500 && !force) {
            return Flowable.empty()
        }
        lastTaskAction = now

        task.isSaving = true
        task.isCreating = true
        task.hasErrored = false
        task.userId = userID
        if (task.id == null) {
            task.id = UUID.randomUUID().toString()
        }
        localRepository.saveSyncronous(task)

        return apiClient.createTask(task)
            .map { task1 ->
                task1.dateCreated = Date()
                task1
            }
            .doOnNext {
                it.tags = task.tags
                localRepository.save(it)
            }
            .doOnError {
                task.hasErrored = true
                task.isSaving = false
                localRepository.saveSyncronous(task)
            }
    }

    @Suppress("ReturnCount")
    override fun updateTask(task: Task, force: Boolean): Maybe<Task> {
        val now = Date().time
        if ((lastTaskAction > now - 500 && !force) || !task.isValid) {
            return Maybe.just(task)
        }
        lastTaskAction = now
        val id = task.id ?: return Maybe.just(task)
        val unmanagedTask = localRepository.getUnmanagedCopy(task)
        unmanagedTask.isSaving = true
        unmanagedTask.hasErrored = false
        localRepository.saveSyncronous(unmanagedTask)
        return apiClient.updateTask(id, unmanagedTask).singleElement()
            .map { task1 ->
                task1.position = task.position
                task1.id = task.id
                task1
            }
            .doOnSuccess {
                it.tags = task.tags
                localRepository.save(it)
            }
            .doOnError {
                unmanagedTask.hasErrored = true
                unmanagedTask.isSaving = false
                localRepository.saveSyncronous(unmanagedTask)
            }
    }

    override fun deleteTask(taskId: String): Flowable<Void> {
        return apiClient.deleteTask(taskId)
            .doOnNext { localRepository.deleteTask(taskId) }
    }

    override fun saveTask(task: Task) {
        localRepository.save(task)
    }

    override fun createTasks(newTasks: List<Task>): Flowable<List<Task>> = apiClient.createTasks(newTasks)

    override fun markTaskCompleted(taskId: String, isCompleted: Boolean) {
        localRepository.markTaskCompleted(taskId, isCompleted)
    }

    override fun <T : BaseMainObject> modify(obj: T, transaction: (T) -> Unit) {
        localRepository.modify(obj, transaction)
    }

    override fun swapTaskPosition(firstPosition: Int, secondPosition: Int) {
        localRepository.swapTaskPosition(firstPosition, secondPosition)
    }

    override fun updateTaskPosition(taskType: TaskType, taskID: String, newPosition: Int): Maybe<List<String>> {
        return apiClient.postTaskNewPosition(taskID, newPosition).firstElement()
            .doOnSuccess { localRepository.updateTaskPositions(it) }
    }

    override fun getUnmanagedTask(taskid: String) = getTask(taskid).map { localRepository.getUnmanagedCopy(it) }

    override fun updateTaskInBackground(task: Task) {
        updateTask(task).subscribe({ }, ExceptionHandler.rx())
    }

    override fun createTaskInBackground(task: Task) {
        createTask(task).subscribe({ }, ExceptionHandler.rx())
    }

    override fun getTaskCopies(userId: String): Flow<List<Task>> =
        localRepository.getTasks(userId).map { localRepository.getUnmanagedCopy(it) }

    override fun getTaskCopies(tasks: List<Task>): List<Task> = localRepository.getUnmanagedCopy(tasks)

    override fun retrieveDailiesFromDate(date: Date): Flowable<TaskList> {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.US)
        return apiClient.getTasks("dailys", formatter.format(date))
    }

    override fun syncErroredTasks(): Single<List<Task>> {
        return localRepository.getErroredTasks(userID).firstElement()
            .flatMapPublisher { Flowable.fromIterable(it) }
            .map { localRepository.getUnmanagedCopy(it) }
            .flatMap {
                return@flatMap if (it.isCreating) {
                    createTask(it, true)
                } else {
                    updateTask(it, true).toFlowable()
                }
            }.toList()
    }

    override fun unlinkAllTasks(challengeID: String?, keepOption: String): Flowable<Void> {
        return apiClient.unlinkAllTasks(challengeID, keepOption)
    }

    override fun getTasksForChallenge(challengeID: String?): Flowable<out List<Task>> {
        return localRepository.getTasksForChallenge(challengeID, userID)
    }
}
