package com.habitrpg.wearos.habitica.data.repositories

import com.habitrpg.wearos.habitica.data.ApiClient
import com.habitrpg.wearos.habitica.models.User
import javax.inject.Inject

class UserRepository @Inject constructor(val apiClient: ApiClient, val localRepository: UserLocalRepository) {

    fun getUser() = localRepository.getUser()

    suspend fun retrieveUser(): User? {
        val user = apiClient.getUser()
        user?.let { localRepository.saveUser(it) }
        return user
    }
    suspend fun updateUser(data: Map<String, Any>): User? {
        val user = apiClient.updateUser(data)
        user?.let { localRepository.saveUser(it) }
        return user
    }

    suspend fun sleep() = apiClient.sleep()
    suspend fun revive() = apiClient.revive()
}