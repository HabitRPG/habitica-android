package com.habitrpg.wearos.habitica.data

import com.habitrpg.common.habitica.models.auth.UserAuth
import com.habitrpg.common.habitica.models.auth.UserAuthResponse
import com.habitrpg.common.habitica.models.auth.UserAuthSocial
import com.habitrpg.common.habitica.models.responses.HabitResponse
import com.habitrpg.common.habitica.models.responses.TaskDirectionData
import com.habitrpg.wearos.habitica.models.User
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
    suspend fun getUser(): HabitResponse<User>

    @PUT("user/")
    suspend fun updateUser(@Body updateDictionary: Map<String, Any>): HabitResponse<User>

    @PUT("user/")
    suspend fun registrationLanguage(@Header("Accept-Language") registrationLanguage: String): HabitResponse<User>

    @GET("tasks/user")
    suspend fun getTasks(@Query("type") type: String): HabitResponse<TaskList>

    @GET("tasks/user")
    suspend fun getTasks(@Query("type") type: String, @Query("dueDate") dueDate: String): HabitResponse<TaskList>

    @GET("tasks/{id}")
    suspend fun getTask(@Path("id") id: String): HabitResponse<Task>

    @POST("tasks/{id}/score/{direction}")
    suspend fun postTaskDirection(@Path("id") id: String, @Path("direction") direction: String): HabitResponse<TaskDirectionData>
    @POST("tasks/bulk-score")
    suspend fun bulkScoreTasks(@Body data: List<Map<String, String>>): HabitResponse<BulkTaskScoringData>

    @POST("tasks/{id}/move/to/{position}")
    suspend fun postTaskNewPosition(@Path("id") id: String, @Path("position") position: Int): HabitResponse<List<String>>

    @POST("tasks/{taskId}/checklist/{itemId}/score")
    suspend fun scoreChecklistItem(@Path("taskId") taskId: String, @Path("itemId") itemId: String): HabitResponse<Task>

    @POST("tasks/user")
    suspend fun createTask(@Body item: Task): HabitResponse<Task>

    @POST("tasks/user")
    suspend fun createTasks(@Body tasks: List<Task>): HabitResponse<List<Task>>

    @PUT("tasks/{id}")
    suspend fun updateTask(@Path("id") id: String, @Body item: Task): HabitResponse<Task>

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String): HabitResponse<Void>

    @POST("user/auth/local/register")
    suspend fun registerUser(@Body auth: UserAuth): HabitResponse<UserAuthResponse>

    @POST("user/auth/local/login")
    suspend fun connectLocal(@Body auth: UserAuth): HabitResponse<UserAuthResponse>

    @POST("user/auth/social")
    suspend fun connectSocial(@Body auth: UserAuthSocial): HabitResponse<UserAuthResponse>

    @DELETE("user/auth/social/{network}")
    suspend fun disconnectSocial(@Path("network") network: String): HabitResponse<Void>

    @POST("user/auth/apple")
    suspend fun loginApple(@Body auth: Map<String, Any>): HabitResponse<UserAuthResponse>

    @POST("user/sleep")
    suspend fun sleep(): HabitResponse<Boolean>

    @POST("user/revive")
    suspend fun revive(): HabitResponse<User>

    // Push notifications
    @POST("user/push-devices")
    suspend fun addPushDevice(@Body pushDeviceData: Map<String, String>): HabitResponse<List<Void>>

    @DELETE("user/push-devices/{regId}")
    suspend fun removePushDevice(@Path("regId") regId: String): HabitResponse<List<Void>>

    @POST("cron")
    suspend fun runCron(): HabitResponse<Void>
}