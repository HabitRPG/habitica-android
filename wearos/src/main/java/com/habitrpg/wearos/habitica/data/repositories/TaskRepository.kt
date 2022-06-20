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

class TaskRepository @Inject constructor(val apiClient: ApiClient, val localRepository: TaskLocalRepository) {

    suspend fun retrieveTasks(): TaskList? {
        val tasks = apiClient.getTasks()
        tasks?.let { localRepository.saveTasks(tasks) }
        return tasks
    }
    fun getTasks(taskType: TaskType): Flow<List<Task>> = localRepository.getTasks(taskType)


    suspend fun scoreTask(user: User?, task: Task, direction: TaskDirection): TaskScoringResult? {
        val id = task.id ?: return null
        val result = apiClient.scoreTask(id, direction.text)
        if (result != null) {
            task.completed = direction == TaskDirection.UP
            localRepository.updateTask(task)
        }
        return result?.let { TaskScoringResult(it, user?.stats) }
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
}