package com.habitrpg.wearos.habitica.data

import com.habitrpg.shared.habitica.models.auth.UserAuthResponse
import com.habitrpg.shared.habitica.models.auth.UserAuthSocial
import com.habitrpg.shared.habitica.models.responses.TaskDirectionData
import com.habitrpg.wearos.habitica.models.EmptyResponse
import com.habitrpg.wearos.habitica.models.WearableHabitResponse
import com.habitrpg.wearos.habitica.models.tasks.Task
import com.habitrpg.wearos.habitica.models.tasks.TaskList
import com.habitrpg.wearos.habitica.models.user.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @GET("user/")
    suspend fun getUser(): Response<WearableHabitResponse<User>>

    @GET("user/")
    @Headers("Cache-Control: no-cache")
    suspend fun getUserForced(): Response<WearableHabitResponse<User>>

    @PUT("user/")
    suspend fun updateUser(
        @Body updateDictionary: Map<String, Any>,
    ): Response<WearableHabitResponse<User>>

    @GET("tasks/user")
    suspend fun getTasks(): Response<WearableHabitResponse<TaskList>>

    @GET("tasks/user")
    @Headers("Cache-Control: no-cache")
    suspend fun getTasksForced(): Response<WearableHabitResponse<TaskList>>

    @POST("tasks/{id}/score/{direction}")
    suspend fun scoreTask(
        @Path("id") id: String,
        @Path("direction") direction: String,
    ): Response<WearableHabitResponse<TaskDirectionData>>

    @POST("tasks/user")
    suspend fun createTask(
        @Body item: Task,
    ): Response<WearableHabitResponse<Task>>

    @POST("user/auth/social")
    suspend fun connectSocial(
        @Body auth: UserAuthSocial,
    ): Response<WearableHabitResponse<UserAuthResponse>>

    @POST("user/sleep")
    suspend fun sleep(): Response<WearableHabitResponse<Boolean>>

    @POST("user/revive")
    suspend fun revive(): Response<WearableHabitResponse<User>>

    @POST("cron")
    suspend fun runCron(): Response<WearableHabitResponse<EmptyResponse>>
}
