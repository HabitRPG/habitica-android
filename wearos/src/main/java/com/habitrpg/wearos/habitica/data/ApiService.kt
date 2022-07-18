package com.habitrpg.wearos.habitica.data

import com.habitrpg.common.habitica.models.auth.UserAuth
import com.habitrpg.common.habitica.models.auth.UserAuthResponse
import com.habitrpg.common.habitica.models.auth.UserAuthSocial
import com.habitrpg.common.habitica.models.responses.TaskDirectionData
import com.habitrpg.wearos.habitica.models.EmptyResponse
import com.habitrpg.wearos.habitica.models.WearableHabitResponse
import com.habitrpg.wearos.habitica.models.tasks.BulkTaskScoringData
import com.habitrpg.wearos.habitica.models.tasks.Task
import com.habitrpg.wearos.habitica.models.tasks.TaskList
import com.habitrpg.wearos.habitica.models.user.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("user/")
    suspend fun getUser(): Response<WearableHabitResponse<User>>
    @GET("user/")
    @Headers("Cache-Control: no-cache")
    suspend fun getUserForced(): Response<WearableHabitResponse<User>>

    @PUT("user/")
    suspend fun updateUser(@Body updateDictionary: Map<String, Any>): Response<WearableHabitResponse<User>>

    @PUT("user/")
    suspend fun registrationLanguage(@Header("Accept-Language") registrationLanguage: String): Response<WearableHabitResponse<User>>

    @GET("tasks/user")
    suspend fun getTasks(): Response<WearableHabitResponse<TaskList>>
    @GET("tasks/user")
    @Headers("Cache-Control: no-cache")
    suspend fun getTasksForced(): Response<WearableHabitResponse<TaskList>>

    @GET("tasks/user")
    suspend fun getTasks(@Query("type") type: String): Response<WearableHabitResponse<TaskList>>

    @GET("tasks/user")
    suspend fun getTasks(@Query("type") type: String, @Query("dueDate") dueDate: String): Response<WearableHabitResponse<TaskList>>

    @GET("tasks/{id}")
    suspend fun getTask(@Path("id") id: String): Response<WearableHabitResponse<Task>>

    @POST("tasks/{id}/score/{direction}")
    suspend fun scoreTask(@Path("id") id: String, @Path("direction") direction: String): Response<WearableHabitResponse<TaskDirectionData>>
    @POST("tasks/bulk-score")
    suspend fun bulkScoreTasks(@Body data: List<Map<String, String>>): Response<WearableHabitResponse<BulkTaskScoringData>>

    @POST("tasks/{id}/move/to/{position}")
    suspend fun postTaskNewPosition(@Path("id") id: String, @Path("position") position: Int): Response<WearableHabitResponse<List<String>>>

    @POST("tasks/{taskId}/checklist/{itemId}/score")
    suspend fun scoreChecklistItem(@Path("taskId") taskId: String, @Path("itemId") itemId: String): Response<WearableHabitResponse<Task>>

    @POST("tasks/user")
    suspend fun createTask(@Body item: Task): Response<WearableHabitResponse<Task>>

    @POST("tasks/user")
    suspend fun createTasks(@Body tasks: List<Task>): Response<WearableHabitResponse<List<Task>>>

    @PUT("tasks/{id}")
    suspend fun updateTask(@Path("id") id: String, @Body item: Task): Response<WearableHabitResponse<Task>>

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String): Response<WearableHabitResponse<Void>>

    @POST("user/auth/local/register")
    suspend fun registerUser(@Body auth: UserAuth): Response<WearableHabitResponse<UserAuthResponse>>

    @POST("user/auth/local/login")
    suspend fun connectLocal(@Body auth: UserAuth): Response<WearableHabitResponse<UserAuthResponse>>

    @POST("user/auth/social")
    suspend fun connectSocial(@Body auth: UserAuthSocial): Response<WearableHabitResponse<UserAuthResponse>>

    @DELETE("user/auth/social/{network}")
    suspend fun disconnectSocial(@Path("network") network: String): Response<WearableHabitResponse<Void>>

    @POST("user/auth/apple")
    suspend fun loginApple(@Body auth: Map<String, Any>): Response<WearableHabitResponse<UserAuthResponse>>

    @POST("user/sleep")
    suspend fun sleep(): Response<WearableHabitResponse<Boolean>>

    @POST("user/revive")
    suspend fun revive(): Response<WearableHabitResponse<User>>

    // Push notifications
    @POST("user/push-devices")
    suspend fun addPushDevice(@Body pushDeviceData: Map<String, String>): Response<WearableHabitResponse<List<Void>>>

    @DELETE("user/push-devices/{regId}")
    suspend fun removePushDevice(@Path("regId") regId: String): Response<WearableHabitResponse<List<Void>>>

    @POST("cron")
    suspend fun runCron(): Response<WearableHabitResponse<EmptyResponse>>
}