package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.shared.habitica.models.tasks.TaskType
import com.habitrpg.shared.habitica.models.tasks.TasksOrder
import io.reactivex.rxjava3.core.Maybe
import kotlinx.coroutines.flow.Flow

interface TaskLocalRepository : BaseLocalRepository {

    fun getTasks(taskType: TaskType, userID: String, includedGroupIDs: Array<String>): Flow<List<Task>>
    fun getTasks(userId: String): Flow<List<Task>>

    fun saveTasks(ownerID: String, tasksOrder: TasksOrder, tasks: TaskList)

    fun deleteTask(taskID: String)

    fun getTask(taskId: String): Flow<Task>
    fun getTaskCopy(taskId: String): Flow<Task>

    fun markTaskCompleted(taskId: String, isCompleted: Boolean)

    fun swapTaskPosition(firstPosition: Int, secondPosition: Int)

    fun getTaskAtPosition(taskType: String, position: Int): Flow<Task>

    fun updateIsdue(daily: TaskList): Maybe<TaskList>

    fun updateTaskPositions(taskOrder: List<String>)
    fun saveCompletedTodos(userId: String, tasks: MutableCollection<Task>)
    fun getErroredTasks(userID: String): Flow<List<Task>>
    fun getUser(userID: String): Flow<User>
    fun getTasksForChallenge(challengeID: String?, userID: String?): Flow<List<Task>>
}
