package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.local.TaskLocalRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.responses.TaskDirection
import com.habitrpg.android.habitica.models.responses.TaskScoringResult
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.RemindersItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.models.tasks.TasksOrder
import com.habitrpg.android.habitica.models.user.User
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import rx.Observable
import rx.functions.Action1
import java.text.SimpleDateFormat
import java.util.*


class TaskRepositoryImpl(localRepository: TaskLocalRepository, apiClient: ApiClient) : BaseRepositoryImpl<TaskLocalRepository>(localRepository, apiClient), TaskRepository {

    private var lastTaskAction: Long = 0

    override fun getTasks(taskType: String, userID: String): Observable<RealmResults<Task>> =
            this.localRepository.getTasks(taskType, userID)

    override fun getTasks(userId: String): Observable<RealmResults<Task>> =
            this.localRepository.getTasks(userId)

    override fun saveTasks(userId: String, order: TasksOrder, tasks: TaskList) {
        localRepository.saveTasks(userId, order, tasks)
    }

    override fun retrieveTasks(userId: String, tasksOrder: TasksOrder): Observable<TaskList> {
        return this.apiClient.getTasks()
                .doOnNext { res -> this.localRepository.saveTasks(userId, tasksOrder, res) }
    }

    override fun retrieveCompletedTodos(userId: String): Observable<TaskList> {
        return this.apiClient.getTasks("completedTodos")
                .doOnNext { taskList ->
                    val tasks = taskList.tasks
                    if (tasks != null) {
                        this.localRepository.saveCompletedTodos(userId, tasks.values)
                    }
                }
    }

    override fun retrieveTasks(userId: String, tasksOrder: TasksOrder, dueDate: Date): Observable<TaskList> {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.US)
        return this.apiClient.getTasks("dailys", formatter.format(dueDate))
                .doOnNext { res -> this.localRepository.saveTasks(userId, tasksOrder, res) }
    }

    override fun taskChecked(user: User?, task: Task, up: Boolean, force: Boolean): Observable<TaskScoringResult?> {
        val now = Date().time
        val id = task.id
        if (lastTaskAction > now - 500 && !force) {
            return Observable.just(null)
        }
        if (id == null) {
            return Observable.just(null)
        }
        lastTaskAction = now
        return this.apiClient.postTaskDirection(id, (if (up) TaskDirection.up else TaskDirection.down).toString())
                .map { res ->
                    // save local task changes
                    val result = TaskScoringResult()
                    if (user != null) {
                        val stats = user.stats

                        result.taskValueDelta = res.delta
                        result.healthDelta = res.hp - stats.getHp()
                        result.experienceDelta = res.exp - stats.getExp()
                        result.manaDelta = res.mp - stats.getMp()
                        result.goldDelta = res.gp - stats.getGp()
                        result.hasLeveledUp = res.lvl > stats.getLvl()
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
                                }
                            }
                            stats.setHp(res.hp)
                            stats.setExp(res.exp)
                            stats.setMp(res.mp)
                            stats.setGp(res.gp)
                            stats.setLvl(res.lvl)
                            user.party?.quest?.progress?.up = (user.party?.quest?.progress?.up ?: 0F) + (res._tmp.quest?.progressDelta?.toFloat() ?: 0F)
                            user.stats = stats
                        }
                    }
                    result
                }
    }

    override fun taskChecked(user: User?, taskId: String, up: Boolean, force: Boolean): Observable<TaskScoringResult?> {
        return localRepository.getTask(taskId).first()
                .flatMap { task -> taskChecked(user, task, up, force) }
    }

    override fun scoreChecklistItem(taskId: String, itemId: String): Observable<Task> {
        return apiClient.scoreChecklistItem(taskId, itemId)
                .flatMap { localRepository.getTask(taskId).first() }
                .doOnNext { task ->
                    val updatedItem: ChecklistItem? = task.checklist.lastOrNull { itemId == it.id }
                    if (updatedItem != null) {
                        localRepository.executeTransaction { updatedItem.completed = !updatedItem.completed }
                    }
                }
    }

    override fun getTask(taskId: String): Observable<Task> = localRepository.getTask(taskId)

    override fun getTaskCopy(taskId: String): Observable<Task> = localRepository.getTaskCopy(taskId)

    override fun createTask(task: Task): Observable<Task> {
        val now = Date().time
        if (lastTaskAction > now - 500) {
            return Observable.just(task)
        }
        lastTaskAction = now
        if (task.tags.size > 0) {
            val tags = RealmList(*localRepository.getUnmanagedCopy(task.tags).toTypedArray())
            task.tags = tags
        }
        if (task.checklist.size > 0) {
            val checklist = RealmList(*localRepository.getUnmanagedCopy(task.checklist).toTypedArray())
            task.checklist = checklist
        }
        if (task.reminders.size > 0) {
            val reminders = RealmList(*localRepository.getUnmanagedCopy(task.reminders).toTypedArray())
            task.reminders = reminders
        }
        return apiClient.createTask(task)
                .map { task1 ->
                    task1.dateCreated = Date()
                    task1
                }
                .doOnNext({ localRepository.saveTask(it) })
    }

    override fun updateTask(task: Task): Observable<Task> {
        val now = Date().time
        if (lastTaskAction > now - 500 || !task.isValid) {
            return Observable.just(task)
        }
        lastTaskAction = now
        val id = task.id
        if (id == null) {
            return Observable.just(task)
        }
        return localRepository.getTaskCopy(id).first()
                .flatMap { task1 -> apiClient.updateTask(id, task1) }
                .map { task1 ->
                    task1.position = task.position
                    task1
                }
                .doOnNext({ localRepository.saveTask(it) })
    }

    override fun deleteTask(taskId: String): Observable<Void> {
        return apiClient.deleteTask(taskId)
                .doOnNext { localRepository.deleteTask(taskId) }
    }

    override fun saveTask(task: Task) {
        localRepository.saveTask(task)
    }

    override fun createTasks(newTasks: List<Task>): Observable<List<Task>> = apiClient.createTasks(newTasks)

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

    override fun updateTaskPosition(taskType: String, oldPosition: Int, newPosition: Int): Observable<List<String>> {
        return localRepository.getTaskAtPosition(taskType, oldPosition)
                .first()
                .flatMap { task ->
                    if (task.isValid) {
                        return@flatMap apiClient.postTaskNewPosition(task.id ?: "", newPosition)
                    }
                    return@flatMap Observable.just<List<String>>(ArrayList())
                }
                .doOnNext({ localRepository.updateTaskPositions(it) })
    }

    override fun getUnmanagedTask(taskid: String): Observable<Task> =
            getTask(taskid).map({ localRepository.getUnmanagedCopy(it) })

    override fun updateTaskInBackground(task: Task) {
        updateTask(task).subscribe(Action1 { }, RxErrorHandler.handleEmptyError())
    }

    override fun createTaskInBackground(task: Task) {
        createTask(task).subscribe(Action1 { }, RxErrorHandler.handleEmptyError())
    }

    override fun getTaskCopies(userId: String): Observable<List<Task>> =
            getTasks(userId).map({ localRepository.getUnmanagedCopy(it) })

    override fun getTaskCopies(tasks: List<Task>): Observable<List<Task>> =
            Observable.just(localRepository.getUnmanagedCopy(tasks))

    override fun updateDailiesIsDue(date: Date): Observable<TaskList> {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.US)
        return apiClient.getTasks("dailys", formatter.format(date))
                .flatMap({ localRepository.updateIsdue(it) })
    }
}
