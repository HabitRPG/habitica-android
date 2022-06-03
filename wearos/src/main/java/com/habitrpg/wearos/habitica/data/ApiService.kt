package com.habitrpg.wearos.habitica.data

import com.habitrpg.common.habitica.models.auth.UserAuth
import com.habitrpg.common.habitica.models.auth.UserAuthResponse
import com.habitrpg.common.habitica.models.auth.UserAuthSocial
import com.habitrpg.common.habitica.models.responses.TaskDirectionData
import com.habitrpg.wearos.habitica.models.User
import com.habitrpg.wearos.habitica.models.WearableHabitResponse
import com.habitrpg.wearos.habitica.models.tasks.BulkTaskScoringData
import com.habitrpg.wearos.habitica.models.tasks.Task
import com.habitrpg.wearos.habitica.models.tasks.TaskList
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("user/")
    suspend fun getUser(): WearableHabitResponse<User>

    @PUT("user/")
    suspend fun updateUser(@Body updateDictionary: Map<String, Any>): WearableHabitResponse<User>

    @PUT("user/")
    suspend fun registrationLanguage(@Header("Accept-Language") registrationLanguage: String): WearableHabitResponse<User>

    @GET("tasks/user")
    suspend fun getTasks(): WearableHabitResponse<TaskList>

    @GET("tasks/user")
    suspend fun getTasks(@Query("type") type: String): WearableHabitResponse<TaskList>

    @GET("tasks/user")
    suspend fun getTasks(@Query("type") type: String, @Query("dueDate") dueDate: String): WearableHabitResponse<TaskList>

    @GET("tasks/{id}")
    suspend fun getTask(@Path("id") id: String): WearableHabitResponse<Task>

    @POST("tasks/{id}/score/{direction}")
    suspend fun postTaskDirection(@Path("id") id: String, @Path("direction") direction: String): WearableHabitResponse<TaskDirectionData>
    @POST("tasks/bulk-score")
    suspend fun bulkScoreTasks(@Body data: List<Map<String, String>>): WearableHabitResponse<BulkTaskScoringData>

    @POST("tasks/{id}/move/to/{position}")
    suspend fun postTaskNewPosition(@Path("id") id: String, @Path("position") position: Int): WearableHabitResponse<List<String>>

    @POST("tasks/{taskId}/checklist/{itemId}/score")
    suspend fun scoreChecklistItem(@Path("taskId") taskId: String, @Path("itemId") itemId: String): WearableHabitResponse<Task>

    @POST("tasks/user")
    suspend fun createTask(@Body item: Task): WearableHabitResponse<Task>

    @POST("tasks/user")
    suspend fun createTasks(@Body tasks: List<Task>): WearableHabitResponse<List<Task>>

    @PUT("tasks/{id}")
    suspend fun updateTask(@Path("id") id: String, @Body item: Task): WearableHabitResponse<Task>

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String): WearableHabitResponse<Void>

    @POST("user/auth/local/register")
    suspend fun registerUser(@Body auth: UserAuth): WearableHabitResponse<UserAuthResponse>

    @POST("user/auth/local/login")
    suspend fun connectLocal(@Body auth: UserAuth): WearableHabitResponse<UserAuthResponse>

    @POST("user/auth/social")
    suspend fun connectSocial(@Body auth: UserAuthSocial): WearableHabitResponse<UserAuthResponse>

    @DELETE("user/auth/social/{network}")
    suspend fun disconnectSocial(@Path("network") network: String): WearableHabitResponse<Void>

    @POST("user/auth/apple")
    suspend fun loginApple(@Body auth: Map<String, Any>): WearableHabitResponse<UserAuthResponse>

    @POST("user/sleep")
    suspend fun sleep(): WearableHabitResponse<Boolean>

    @POST("user/revive")
    suspend fun revive(): WearableHabitResponse<User>

    // Push notifications
    @POST("user/push-devices")
    suspend fun addPushDevice(@Body pushDeviceData: Map<String, String>): WearableHabitResponse<List<Void>>

    @DELETE("user/push-devices/{regId}")
    suspend fun removePushDevice(@Path("regId") regId: String): WearableHabitResponse<List<Void>>

    @POST("cron")
    suspend fun runCron(): WearableHabitResponse<Void>
}