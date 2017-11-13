package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.tasks.RemindersItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.models.tasks.TasksOrder
import io.realm.RealmResults
import rx.Observable

interface TaskLocalRepository : BaseLocalRepository {

    fun getTasks(taskType: String, userID: String): Observable<RealmResults<Task>>
    fun getTasks(userId: String): Observable<RealmResults<Task>>

    fun saveTasks(userId: String, tasksOrder: TasksOrder, tasks: TaskList)

    fun saveTask(task: Task)

    fun deleteTask(taskID: String)

    fun getTask(taskId: String): Observable<Task>
    fun getTaskCopy(taskId: String): Observable<Task>

    fun markTaskCompleted(taskId: String, isCompleted: Boolean)

    fun saveReminder(remindersItem: RemindersItem)

    fun swapTaskPosition(firstPosition: Int, secondPosition: Int)

    fun getTaskAtPosition(currentPosition: Int): Observable<Task>

    fun updateIsdue(daily: TaskList): Observable<TaskList>

    fun updateTaskPositions(taskOrder: List<String>)
}
