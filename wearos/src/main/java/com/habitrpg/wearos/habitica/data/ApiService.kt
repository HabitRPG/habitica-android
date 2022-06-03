package com.habitrpg.wearos.habitica.data

import com.habitrpg.common.habitica.models.responses.HabitResponse
import com.habitrpg.wearos.habitica.models.User
import retrofit2.http.GET

interface ApiService {
    @GET("user/")
    suspend fun getUser(): HabitResponse<User>
}