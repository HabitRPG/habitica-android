package com.habitrpg.wearos.habitica.data.repositories

import com.habitrpg.wearos.habitica.data.ApiClient
import javax.inject.Inject

class TaskRepository @Inject constructor(val apiClient: ApiClient, val localRepository: TaskLocalRepository) {

    suspend fun retrieveTasks() = apiClient.getTasks()

}