package com.habitrpg.wearos.habitica.data.repositories

import com.habitrpg.wearos.habitica.data.ApiClient
import com.habitrpg.wearos.habitica.models.user.User
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val apiClient: ApiClient,
    private val localRepository: UserLocalRepository
) {
    val hasAuthentication: Boolean
        get() = apiClient.hasAuthentication()

    fun getUser() = localRepository.getUser()

    suspend fun retrieveUser(forced: Boolean = false): User? {
        val user = apiClient.getUser(forced)
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
    suspend fun runCron() {
        apiClient.runCron()
    }
}