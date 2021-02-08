package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.models.responses.BulkTaskScoringData
import com.habitrpg.android.habitica.models.responses.TaskScoringResult
import com.habitrpg.android.habitica.models.tasks.RemindersItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.models.tasks.TasksOrder
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.realm.Realm
import io.realm.RealmResults
import java.util.*

interface TaskRepository : BaseRepository {
    fun getTasks(taskType: String, userID: String): Flowable<RealmResults<Task>>
    fun getTasks(userId: String): Flowable<RealmResults<Task>>
    fun getCurrentUserTasks(taskType: String): Flowable<RealmResults<Task>>
    fun getTasksOfType(taskType: String): Flowable<RealmResults<Task>>
    fun saveTasks(userId: String, order: TasksOrder, tasks: TaskList)

    fun retrieveTasks(userId: String, tasksOrder: TasksOrder): Flowable<TaskList>
    fun retrieveTasks(userId: String, tasksOrder: TasksOrder, dueDate: Date): Flowable<TaskList>

    fun taskChecked(user: User?, task: Task, up: Boolean, force: Boolean, notifyFunc: ((TaskScoringResult) -> Unit)?): Flowable<TaskScoringResult?>
    fun taskChecked(user: User?, taskId: String, up: Boolean, force: Boolean, notifyFunc: ((TaskScoringResult) -> Unit)?): Maybe<TaskScoringResult?>
    fun scoreChecklistItem(taskId: String, itemId: String): Flowable<Task>

    fun getTask(taskId: String): Flowable<Task>
    fun getTaskCopy(taskId: String): Flowable<Task>
    fun createTask(task: Task, force: Boolean = false): Flowable<Task>
    fun updateTask(task: Task, force: Boolean = false): Maybe<Task>?
    fun deleteTask(taskId: String): Flowable<Void>
    fun saveTask(task: Task)

    fun createTasks(newTasks: List<Task>): Flowable<List<Task>>

    fun markTaskCompleted(taskId: String, isCompleted: Boolean)

    fun saveReminder(remindersItem: RemindersItem)

    fun <T: BaseObject> modify(obj: T, transaction: (T) -> Unit)

    fun swapTaskPosition(firstPosition: Int, secondPosition: Int)

    fun updateTaskPosition(taskType: String, taskID: String, newPosition: Int): Maybe<List<String>>

    fun getUnmanagedTask(taskid: String): Flowable<Task>

    fun updateTaskInBackground(task: Task)

    fun createTaskInBackground(task: Task)

    fun getTaskCopies(userId: String): Flowable<List<Task>>

    fun getTaskCopies(tasks: List<Task>): Flowable<List<Task>>

    fun retrieveDailiesFromDate(date: Date): Flowable<TaskList>
    fun retrieveCompletedTodos(userId: String): Flowable<TaskList>
    fun syncErroredTasks(): Single<List<Task>>
    fun unlinkAllTasks(challengeID: String?, keepOption: String): Flowable<Void>
    fun getTasksForChallenge(challengeID: String?): Flowable<RealmResults<Task>>
    fun bulkScoreTasks(data: List<Map<String, String>>): Flowable<BulkTaskScoringData>
}
