package com.habitrpg.wearos.habitica.data.repositories

import com.habitrpg.common.habitica.models.responses.TaskDirection
import com.habitrpg.common.habitica.models.responses.TaskScoringResult
import com.habitrpg.common.habitica.models.tasks.TaskType
import com.habitrpg.wearos.habitica.data.ApiClient
import com.habitrpg.wearos.habitica.models.tasks.Task
import com.habitrpg.wearos.habitica.models.tasks.TaskList
import com.habitrpg.wearos.habitica.models.user.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

class TaskRepository @Inject constructor(val apiClient: ApiClient, val localRepository: TaskLocalRepository, val userLocalRepository: UserLocalRepository) {

    suspend fun retrieveTasks(): TaskList? {
        val tasks = apiClient.getTasks()
        tasks?.let { localRepository.saveTasks(tasks) }
        return tasks
    }
    fun getTasks(taskType: TaskType) = localRepository.getTasks(taskType)

    suspend fun scoreTask(user: User?, task: Task, direction: TaskDirection): TaskScoringResult? {
        val id = task.id ?: return null
        val result = apiClient.scoreTask(id, direction.text)
        if (result != null) {
            task.completed = direction == TaskDirection.UP
            task.value += result.delta
            if (task.type == TaskType.HABIT) {
                if (direction == TaskDirection.UP) {
                    task.counterUp = task.counterUp?.plus(1) ?: 1
                } else {
                    task.counterUp = task.counterDown?.plus(1) ?: 1
                }
            } else if (task.type == TaskType.DAILY) {
                if (direction == TaskDirection.UP) {
                    task.streak = task.streak?.plus(1) ?: 1
                } else {
                    task.streak = task.streak?.minus(1) ?: 0
                }
            }
            localRepository.updateTask(task)
        }
        val scoringResult = result?.let { TaskScoringResult(it, user?.stats) }
        if (user != null) {
            user.stats?.hp = result?.hp
            user.stats?.exp = result?.exp
            user.stats?.mp = result?.mp
            user.stats?.gp = result?.gp
            user.stats?.lvl = result?.lvl
        /*user?.party?.quest?.progress?.up = (
            user?.party?.quest?.progress?.up
                ?: 0F
            ) + (result?._tmp?.quest?.progressDelta?.toFloat() ?: 0F)*/
            userLocalRepository.saveUser(user)
        }
        return scoringResult
    }

    fun getTask(taskID: String?): Flow<Task?> {
        if (taskID == null) return emptyFlow()
        return localRepository.getTask(taskID)
    }

    suspend fun createTask(task: Task) {
        val newTask = apiClient.createTask(task)
        if (newTask != null) {
            localRepository.updateTask(newTask)
        }
    }

    fun getTaskCounts() = localRepository.getTaskCounts()
    fun getActiveTaskCounts() = localRepository.getActiveTaskCounts()
}