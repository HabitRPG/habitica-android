package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.tasks.RemindersItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.models.tasks.TasksOrder
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.realm.RealmResults

interface TaskLocalRepository : BaseLocalRepository {

    fun getTasks(taskType: String, userID: String): Flowable<RealmResults<Task>>
    fun getTasks(userId: String): Flowable<RealmResults<Task>>

    fun saveTasks(userId: String, tasksOrder: TasksOrder, tasks: TaskList)

    fun deleteTask(taskID: String)

    fun getTask(taskId: String): Flowable<Task>
    fun getTaskCopy(taskId: String): Flowable<Task>

    fun markTaskCompleted(taskId: String, isCompleted: Boolean)

    fun saveReminder(remindersItem: RemindersItem)

    fun swapTaskPosition(firstPosition: Int, secondPosition: Int)

    fun getTaskAtPosition(taskType: String, position: Int): Flowable<Task>

    fun updateIsdue(daily: TaskList): Maybe<TaskList>

    fun updateTaskPositions(taskOrder: List<String>)
    fun saveCompletedTodos(userId: String, tasks: MutableCollection<Task>)
    fun getErroredTasks(userID: String): Flowable<RealmResults<Task>>
    fun getUser(userID: String): Flowable<User>
    fun getTasksForChallenge(challengeID: String?, userID: String?): Flowable<RealmResults<Task>>
}
