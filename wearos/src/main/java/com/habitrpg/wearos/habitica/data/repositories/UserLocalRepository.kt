package com.habitrpg.wearos.habitica.data.repositories

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import com.habitrpg.wearos.habitica.models.user.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserLocalRepository @Inject constructor() {
    private val user = MutableLiveData<User?>()
    fun getUser() = user.asFlow()

    fun saveUser(user: User) {
        this.user.value = user
    }

    fun clearData() {
        user.value = null
    }
}