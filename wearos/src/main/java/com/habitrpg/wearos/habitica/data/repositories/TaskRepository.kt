package com.habitrpg.wearos.habitica.data.repositories

import com.habitrpg.common.habitica.models.responses.TaskDirection
import com.habitrpg.common.habitica.models.responses.TaskScoringResult
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


    suspend fun scoreTask(task: Task, direction: TaskDirection): TaskScoringResult? {
        val id = task.id ?: return null
        val result = apiClient.scoreTask(id, direction.text)
        if (result != null) {
            task.completed = direction == TaskDirection.UP
            localRepository.updateTask(task)
        }
        return result?.let { TaskScoringResult(it, null) }
    }
}