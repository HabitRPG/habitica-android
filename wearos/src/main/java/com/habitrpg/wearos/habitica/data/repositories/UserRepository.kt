package com.habitrpg.wearos.habitica.data.repositories

import com.habitrpg.wearos.habitica.data.ApiClient
import com.habitrpg.wearos.habitica.models.NetworkResult
import com.habitrpg.wearos.habitica.models.user.User
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val apiClient: ApiClient,
    private val localRepository: UserLocalRepository
) {
    val userID: String
    get() = apiClient.userID
    val hasAuthentication: Boolean
        get() = apiClient.hasAuthentication()

    fun getUser() = localRepository.getUser()

    suspend fun retrieveUser(ensureFresh: Boolean = false): User? {
        var response = apiClient.getUser()
        var user = (response as? NetworkResult.Success)?.data
        user?.let { localRepository.saveUser(it) }
        if (ensureFresh && !response.isResponseFresh) {
            response = apiClient.getUser(true)
            user = (response as? NetworkResult.Success)?.data
            user?.let { localRepository.saveUser(it) }
        }
        return user
    }

    suspend fun updateUser(data: Map<String, Any>): User? {
        val user = apiClient.updateUser(data).responseData
        user?.let { localRepository.saveUser(it) }
        return user
    }

    suspend fun sleep() = apiClient.sleep().responseData
    suspend fun revive() = apiClient.revive().responseData
    suspend fun runCron() {
        apiClient.runCron()
    }

    fun clearData() {
        localRepository.clearData()
    }
}