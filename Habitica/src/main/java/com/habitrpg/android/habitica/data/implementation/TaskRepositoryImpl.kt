package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.local.TaskLocalRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.responses.TaskDirection
import com.habitrpg.android.habitica.models.responses.TaskScoringResult
import com.habitrpg.android.habitica.models.tasks.*
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.functions.Consumer
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import java.text.SimpleDateFormat
import java.util.*


class TaskRepositoryImpl(localRepository: TaskLocalRepository, apiClient: ApiClient, userID: String) : BaseRepositoryImpl<TaskLocalRepository>(localRepository, apiClient, userID), TaskRepository {
    override fun getTasksOfType(taskType: String): Flowable<RealmResults<Task>> = getTasks(taskType, userID)

    private var lastTaskAction: Long = 0

    override fun getTasks(taskType: String, userID: String): Flowable<RealmResults<Task>> =
            this.localRepository.getTasks(taskType, userID)

    override fun getTasks(userId: String): Flowable<RealmResults<Task>> =
            this.localRepository.getTasks(userId)

    override fun saveTasks(userId: String, order: TasksOrder, tasks: TaskList) {
        localRepository.saveTasks(userId, order, tasks)
    }

    override fun retrieveTasks(userId: String, tasksOrder: TasksOrder): Flowable<TaskList> {
        return this.apiClient.tasks
                .doOnNext { res -> this.localRepository.saveTasks(userId, tasksOrder, res) }
    }

    override fun retrieveCompletedTodos(userId: String): Flowable<TaskList> {
        return this.apiClient.getTasks("completedTodos")
                .doOnNext { taskList ->
                    val tasks = taskList.tasks
                    if (tasks != null) {
                        this.localRepository.saveCompletedTodos(userId, tasks.values)
                    }
                }
    }

    override fun retrieveTasks(userId: String, tasksOrder: TasksOrder, dueDate: Date): Flowable<TaskList> {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.US)
        return this.apiClient.getTasks("dailys", formatter.format(dueDate))
                .doOnNext { res -> this.localRepository.saveTasks(userId, tasksOrder, res) }
    }

    @Suppress("ReturnCount")
    override fun taskChecked(user: User?, task: Task, up: Boolean, force: Boolean): Flowable<TaskScoringResult?> {
        val now = Date().time
        val id = task.id
        if (lastTaskAction > now - 500 && !force || id == null) {
            return Flowable.empty()
        }
        lastTaskAction = now
        return this.apiClient.postTaskDirection(id, (if (up) TaskDirection.up else TaskDirection.down).toString())
                .map { res ->
                    // save local task changes
                    val result = TaskScoringResult()
                    if (user != null) {
                        val stats = user.stats

                        result.taskValueDelta = res.delta
                        result.healthDelta = res.hp - (stats?.hp ?: 0.0)
                        result.experienceDelta = res.exp - (stats?.exp ?: 0.0)
                        result.manaDelta = res.mp - (stats?.mp ?: 0.0)
                        result.goldDelta = res.gp - (stats?.gp ?: 0.0)
                        result.hasLeveledUp = res.lvl > stats?.lvl ?: 0
                        result.questDamage = res._tmp.quest?.progressDelta
                        if (res._tmp != null) {
                            result.drop = res._tmp.drop
                        }
                        this.localRepository.executeTransaction {
                            if (!task.isValid) {
                                return@executeTransaction
                            }
                            if (task.type != "reward") {
                                task.value = task.value + res.delta
                                if (Task.TYPE_DAILY == task.type || Task.TYPE_TODO == task.type) {
                                    task.completed = up
                                    if (Task.TYPE_DAILY == task.type) {
                                        task.streak = (task.streak ?: 0) + 1
                                    }
                                } else if (Task.TYPE_HABIT == task.type) {
                                    if (up) {
                                        task.counterUp = (task.counterUp ?: 0) + 1
                                    } else {
                                        task.counterDown = (task.counterDown ?: 0) + 1
                                    }
                                }
                            }
                            stats?.hp = res.hp
                            stats?.exp = res.exp
                            stats?.mp = res.mp
                            stats?.gp = res.gp
                            stats?.lvl = res.lvl
                            user.party?.quest?.progress?.up = (user.party?.quest?.progress?.up ?: 0F) + (res._tmp.quest?.progressDelta?.toFloat() ?: 0F)
                            user.stats = stats
                        }
                    }
                    result
                }
    }

    override fun taskChecked(user: User?, taskId: String, up: Boolean, force: Boolean): Maybe<TaskScoringResult?> {
        return localRepository.getTask(taskId).firstElement()
                .flatMap { task -> taskChecked(user, task, up, force).singleElement() }
    }

    override fun scoreChecklistItem(taskId: String, itemId: String): Flowable<Task> {
        return apiClient.scoreChecklistItem(taskId, itemId)
                .flatMapMaybe { localRepository.getTask(taskId).firstElement() }
                .doOnNext { task ->
                    val updatedItem: ChecklistItem? = task.checklist?.lastOrNull { itemId == it.id }
                    if (updatedItem != null) {
                        localRepository.executeTransaction { updatedItem.completed = !updatedItem.completed }
                    }
                }
    }

    override fun getTask(taskId: String): Flowable<Task> = localRepository.getTask(taskId)

    override fun getTaskCopy(taskId: String): Flowable<Task> = localRepository.getTaskCopy(taskId)

    override fun createTask(task: Task): Flowable<Task> {
        val now = Date().time
        if (lastTaskAction > now - 500) {
            return Flowable.empty()
        }
        lastTaskAction = now
        task.tags?.let {
            if (it.size > 0) {
                val tags = RealmList<Tag>()
                tags.addAll(localRepository.getUnmanagedCopy(it))
                task.tags = tags
            }
        }

        task.checklist?.let {
            if (it.size > 0) {
                val checklist = RealmList<ChecklistItem>()
                checklist.addAll(localRepository.getUnmanagedCopy(it))
                task.checklist = checklist
            }
        }

        task.reminders?.let {
            if (it.size > 0) {
                val reminders = RealmList<RemindersItem>()
                reminders.addAll(localRepository.getUnmanagedCopy(it))
                task.reminders = reminders
            }
        }

        return apiClient.createTask(task)
                .map { task1 ->
                    task1.dateCreated = Date()
                    task1
                }
                .doOnNext { localRepository.saveTask(it) }
    }

    @Suppress("ReturnCount")
    override fun updateTask(task: Task): Maybe<Task> {
        val now = Date().time
        if (lastTaskAction > now - 500 || !task.isValid) {
            return Maybe.just(task)
        }
        lastTaskAction = now
        val id = task.id ?: return Maybe.just(task)
        return localRepository.getTaskCopy(id).firstElement()
                .flatMap { task1 -> apiClient.updateTask(id, task1).singleElement() }
                .map { task1 ->
                    task1.position = task.position
                    task1
                }
                .doOnSuccess { localRepository.saveTask(it) }
    }

    override fun deleteTask(taskId: String): Flowable<Void> {
        return apiClient.deleteTask(taskId)
                .doOnNext { localRepository.deleteTask(taskId) }
    }

    override fun saveTask(task: Task) {
        localRepository.saveTask(task)
    }

    override fun createTasks(newTasks: List<Task>): Flowable<List<Task>> = apiClient.createTasks(newTasks)

    override fun markTaskCompleted(taskId: String, isCompleted: Boolean) {
        localRepository.markTaskCompleted(taskId, isCompleted)
    }

    override fun saveReminder(remindersItem: RemindersItem) {
        localRepository.saveReminder(remindersItem)
    }

    override fun executeTransaction(transaction: Realm.Transaction) {
        localRepository.executeTransaction(transaction)
    }

    override fun swapTaskPosition(firstPosition: Int, secondPosition: Int) {
        localRepository.swapTaskPosition(firstPosition, secondPosition)
    }

    override fun updateTaskPosition(taskType: String, oldPosition: Int, newPosition: Int): Maybe<List<String>> {
        return localRepository.getTaskAtPosition(taskType, oldPosition)
                .firstElement()
                .flatMap { task ->
                    return@flatMap if (task.isValid) {
                         apiClient.postTaskNewPosition(task.id ?: "", newPosition).firstElement()
                    } else {
                        Maybe.just<List<String>>(ArrayList())
                    }
                }
                .doOnSuccess { localRepository.updateTaskPositions(it) }
    }

    override fun getUnmanagedTask(taskid: String): Flowable<Task> =
            getTask(taskid).map { localRepository.getUnmanagedCopy(it) }

    override fun updateTaskInBackground(task: Task) {
        updateTask(task).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
    }

    override fun createTaskInBackground(task: Task) {
        createTask(task).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
    }

    override fun getTaskCopies(userId: String): Flowable<List<Task>> =
            getTasks(userId).map { localRepository.getUnmanagedCopy(it) }

    override fun getTaskCopies(tasks: List<Task>): Flowable<List<Task>> =
            Flowable.just(localRepository.getUnmanagedCopy(tasks))

    override fun updateDailiesIsDue(date: Date): Flowable<TaskList> {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.US)
        return apiClient.getTasks("dailys", formatter.format(date))
                .flatMapMaybe { localRepository.updateIsdue(it) }
    }
}
