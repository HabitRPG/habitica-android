package com.habitrpg.wearos.habitica.data.repositories

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import com.habitrpg.wearos.habitica.models.user.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserLocalRepository @Inject constructor() {
    private val user = MutableLiveData<User>()
    fun getUser(): Flow<User> {
        return user.asFlow()
    }

    fun saveUser(user: User) {
        this.user.value = user
    }
}