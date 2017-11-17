package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.responses.TaskScoringResult
import com.habitrpg.android.habitica.models.tasks.RemindersItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.models.tasks.TasksOrder
import com.habitrpg.android.habitica.models.user.User
import io.realm.Realm
import io.realm.RealmResults
import rx.Observable
import java.util.*

interface TaskRepository : BaseRepository {
    fun getTasks(taskType: String, userID: String): Observable<RealmResults<Task>>
    fun getTasks(userId: String): Observable<RealmResults<Task>>
    fun saveTasks(userId: String, order: TasksOrder, tasks: TaskList)

    fun retrieveTasks(userId: String, tasksOrder: TasksOrder): Observable<TaskList>
    fun retrieveTasks(userId: String, tasksOrder: TasksOrder, dueDate: Date): Observable<TaskList>

    fun taskChecked(user: User?, task: Task, up: Boolean, force: Boolean): Observable<TaskScoringResult?>
    fun taskChecked(user: User?, taskId: String, up: Boolean, force: Boolean): Observable<TaskScoringResult?>
    fun scoreChecklistItem(taskId: String, itemId: String): Observable<Task>

    fun getTask(taskId: String): Observable<Task>
    fun getTaskCopy(taskId: String): Observable<Task>
    fun createTask(task: Task): Observable<Task>
    fun updateTask(task: Task): Observable<Task>
    fun deleteTask(taskId: String): Observable<Void>
    fun saveTask(task: Task)

    fun createTasks(newTasks: List<Task>): Observable<List<Task>>

    fun markTaskCompleted(taskId: String, isCompleted: Boolean)

    fun saveReminder(remindersItem: RemindersItem)

    fun executeTransaction(transaction: Realm.Transaction)

    fun swapTaskPosition(firstPosition: Int, secondPosition: Int)

    fun updateTaskPosition(taskType: String, oldPosition: Int, newPosition: Int): Observable<List<String>>

    fun getUnmanagedTask(taskid: String): Observable<Task>

    fun updateTaskInBackground(task: Task)

    fun createTaskInBackground(task: Task)

    fun getTaskCopies(userId: String): Observable<List<Task>>

    fun getTaskCopies(tasks: List<Task>): Observable<List<Task>>

    fun updateDailiesIsDue(date: Date): Observable<TaskList>
}
