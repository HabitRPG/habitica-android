package com.habitrpg.wearos.habitica.data.repositories

import com.habitrpg.wearos.habitica.data.ApiClient
import javax.inject.Inject

class UserRepository @Inject constructor(val apiClient: ApiClient, val localRepository: UserLocalRepository) {

    suspend fun retrieveUser() = apiClient.getUser()
    suspend fun updateUser(data: Map<String, Any>) = apiClient.updateUser(data)

    suspend fun sleep() = apiClient.sleep()
    suspend fun revive() = apiClient.revive()
}