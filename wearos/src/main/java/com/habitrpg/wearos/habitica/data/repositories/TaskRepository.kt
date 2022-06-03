package com.habitrpg.wearos.habitica.data.repositories

import com.habitrpg.common.habitica.models.responses.TaskDirection
import com.habitrpg.common.habitica.models.tasks.TaskType
import com.habitrpg.wearos.habitica.data.ApiClient
import com.habitrpg.wearos.habitica.models.tasks.Task
import com.habitrpg.wearos.habitica.models.tasks.TaskList
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TaskRepository @Inject constructor(val apiClient: ApiClient, val localRepository: TaskLocalRepository) {

    suspend fun retrieveTasks(): TaskList? {
        val tasks = apiClient.getTasks()
        tasks?.let { localRepository.saveTasks(tasks) }
        return tasks
    }
    fun getTasks(taskType: TaskType): Flow<List<Task>> = localRepository.getTasks(taskType)

    suspend fun scoreTask(task: Task, direction: TaskDirection) {
        val id = task.id ?: return
        val response = apiClient.scoreTask(id, direction.text)
    }
}