package com.habitrpg.android.habitica.api

import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.LeaveChallengeBody
import com.habitrpg.android.habitica.models.PurchaseValidationRequest
import com.habitrpg.android.habitica.models.PurchaseValidationResult
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.TeamPlan
import com.habitrpg.android.habitica.models.WorldState
import com.habitrpg.android.habitica.models.auth.UserAuth
import com.habitrpg.android.habitica.models.auth.UserAuthResponse
import com.habitrpg.android.habitica.models.auth.UserAuthSocial
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.responses.BulkTaskScoringData
import com.habitrpg.android.habitica.models.responses.BuyResponse
import com.habitrpg.android.habitica.models.responses.FeedResponse
import com.habitrpg.android.habitica.models.responses.PostChatMessageResult
import com.habitrpg.android.habitica.models.responses.SkillResponse
import com.habitrpg.android.habitica.models.responses.Status
import com.habitrpg.android.habitica.models.responses.UnlockResponse
import com.habitrpg.android.habitica.models.responses.VerifyUsernameResponse
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.FindUsernameResult
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.social.InboxConversation
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.models.user.Items
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.common.habitica.models.responses.HabitResponse
import com.habitrpg.common.habitica.models.responses.TaskDirectionData
import io.reactivex.rxjava3.core.Flowable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

@JvmSuppressWildcards
interface ApiService {
    @get:GET("status")
    val status: Flowable<HabitResponse<Status>>

    /* user API */

    @get:GET("user/")
    val user: Flowable<HabitResponse<User>>

    @GET("inbox/messages")
    fun getInboxMessages(@Query("conversation") uuid: String, @Query("page") page: Int): Flowable<HabitResponse<List<ChatMessage>>>
    @GET("inbox/conversations")
    fun getInboxConversations(): Flowable<HabitResponse<List<InboxConversation>>>

    @get:GET("tasks/user")
    val tasks: Flowable<HabitResponse<TaskList>>

    @get:GET("world-state")
    val worldState: Flowable<HabitResponse<WorldState>>

    @GET("content")
    fun getContent(@Query("language") language: String?): Flowable<HabitResponse<ContentResult>>

    @PUT("user/")
    fun updateUser(@Body updateDictionary: Map<String, Any>): Flowable<HabitResponse<User>>

    @PUT("user/")
    fun registrationLanguage(@Header("Accept-Language") registrationLanguage: String): Flowable<HabitResponse<User>>

    @GET("user/in-app-rewards")
    fun retrieveInAppRewards(): Flowable<HabitResponse<List<ShopItem>>>

    @GET("user/inventory/buy")
    fun retrieveOldGearRewards(): Flowable<HabitResponse<List<ShopItem>>>

    @POST("user/equip/{type}/{key}")
    fun equipItem(@Path("type") type: String, @Path("key") itemKey: String): Flowable<HabitResponse<Items>>

    @POST("user/buy/{key}")
    fun buyItem(@Path("key") itemKey: String, @Body quantity: Map<String, Int>): Flowable<HabitResponse<BuyResponse>>

    @POST("user/purchase/{type}/{key}")
    fun purchaseItem(
        @Path("type") type: String,
        @Path("key") itemKey: String,
        @Body quantity: Map<String, Int>
    ): Flowable<HabitResponse<Void>>

    @POST("user/purchase-hourglass/{type}/{key}")
    fun purchaseHourglassItem(@Path("type") type: String, @Path("key") itemKey: String): Flowable<HabitResponse<Void>>

    @POST("user/buy-mystery-set/{key}")
    fun purchaseMysterySet(@Path("key") itemKey: String): Flowable<HabitResponse<Void>>

    @POST("user/buy-quest/{key}")
    fun purchaseQuest(@Path("key") key: String): Flowable<HabitResponse<Void>>

    @POST("user/buy-special-spell/{key}")
    fun purchaseSpecialSpell(@Path("key") key: String): Flowable<HabitResponse<Void>>

    @POST("user/sell/{type}/{key}")
    fun sellItem(@Path("type") itemType: String, @Path("key") itemKey: String): Flowable<HabitResponse<User>>

    @POST("user/feed/{pet}/{food}")
    fun feedPet(@Path("pet") petKey: String, @Path("food") foodKey: String): Flowable<HabitResponse<FeedResponse>>

    @POST("user/hatch/{egg}/{hatchingPotion}")
    fun hatchPet(@Path("egg") eggKey: String, @Path("hatchingPotion") hatchingPotionKey: String): Flowable<HabitResponse<Items>>

    @GET("tasks/user")
    fun getTasks(@Query("type") type: String): Flowable<HabitResponse<TaskList>>

    @GET("tasks/user")
    fun getTasks(@Query("type") type: String, @Query("dueDate") dueDate: String): Flowable<HabitResponse<TaskList>>

    @POST("user/unlock")
    fun unlockPath(@Query("path") path: String): Flowable<HabitResponse<UnlockResponse>>

    @GET("tasks/{id}")
    fun getTask(@Path("id") id: String): Flowable<HabitResponse<Task>>

    @POST("tasks/{id}/score/{direction}")
    fun postTaskDirection(@Path("id") id: String, @Path("direction") direction: String): Flowable<HabitResponse<TaskDirectionData>>
    @POST("tasks/bulk-score")
    fun bulkScoreTasks(@Body data: List<Map<String, String>>): Flowable<HabitResponse<BulkTaskScoringData>>

    @POST("tasks/{id}/move/to/{position}")
    fun postTaskNewPosition(@Path("id") id: String, @Path("position") position: Int): Flowable<HabitResponse<List<String>>>

    @POST("tasks/{taskId}/checklist/{itemId}/score")
    fun scoreChecklistItem(@Path("taskId") taskId: String, @Path("itemId") itemId: String): Flowable<HabitResponse<Task>>

    @POST("tasks/user")
    fun createTask(@Body item: Task): Flowable<HabitResponse<Task>>

    @POST("tasks/user")
    fun createTasks(@Body tasks: List<Task>): Flowable<HabitResponse<List<Task>>>

    @PUT("tasks/{id}")
    fun updateTask(@Path("id") id: String, @Body item: Task): Flowable<HabitResponse<Task>>

    @DELETE("tasks/{id}")
    fun deleteTask(@Path("id") id: String): Flowable<HabitResponse<Void>>

    @POST("tags")
    fun createTag(@Body tag: Tag): Flowable<HabitResponse<Tag>>

    @PUT("tags/{id}")
    fun updateTag(@Path("id") id: String, @Body tag: Tag): Flowable<HabitResponse<Tag>>

    @DELETE("tags/{id}")
    fun deleteTag(@Path("id") id: String): Flowable<HabitResponse<Void>>

    @POST("user/auth/local/register")
    fun registerUser(@Body auth: UserAuth): Flowable<HabitResponse<UserAuthResponse>>

    @POST("user/auth/local/login")
    fun connectLocal(@Body auth: UserAuth): Flowable<HabitResponse<UserAuthResponse>>

    @POST("user/auth/social")
    fun connectSocial(@Body auth: UserAuthSocial): Flowable<HabitResponse<UserAuthResponse>>

    @DELETE("user/auth/social/{network}")
    fun disconnectSocial(@Path("network") network: String): Flowable<HabitResponse<Void>>

    @POST("user/auth/apple")
    fun loginApple(@Body auth: Map<String, Any>): Flowable<HabitResponse<UserAuthResponse>>

    @POST("user/sleep")
    fun sleep(): Flowable<HabitResponse<Boolean>>

    @POST("user/revive")
    fun revive(): Flowable<HabitResponse<User>>

    @POST("user/class/cast/{skill}")
    fun useSkill(
        @Path("skill") skillName: String,
        @Query("targetType") targetType: String,
        @Query("targetId") targetId: String
    ): Flowable<HabitResponse<SkillResponse>>

    @POST("user/class/cast/{skill}")
    fun useSkill(@Path("skill") skillName: String, @Query("targetType") targetType: String): Flowable<HabitResponse<SkillResponse>>

    @POST("user/change-class")
    fun changeClass(): Flowable<HabitResponse<User>>

    @POST("user/change-class")
    fun changeClass(@Query("class") className: String): Flowable<HabitResponse<User>>

    @POST("user/disable-classes")
    fun disableClasses(): Flowable<HabitResponse<User>>

    @POST("user/mark-pms-read")
    fun markPrivateMessagesRead(): Flowable<Void>

    /* Group API */

    @GET("groups")
    fun listGroups(@Query("type") type: String): Flowable<HabitResponse<List<Group>>>

    @GET("groups/{gid}")
    fun getGroup(@Path("gid") groupId: String): Flowable<HabitResponse<Group>>

    @POST("groups")
    fun createGroup(@Body item: Group): Flowable<HabitResponse<Group>>

    @PUT("groups/{id}")
    fun updateGroup(@Path("id") id: String, @Body item: Group): Flowable<HabitResponse<Group>>

    @POST("groups/{groupID}/removeMember/{userID}")
    fun removeMemberFromGroup(@Path("groupID") groupID: String, @Path("userID") userID: String): Flowable<HabitResponse<Void>>

    @GET("groups/{gid}/chat")
    fun listGroupChat(@Path("gid") groupId: String): Flowable<HabitResponse<List<ChatMessage>>>

    @POST("groups/{gid}/join")
    fun joinGroup(@Path("gid") groupId: String): Flowable<HabitResponse<Group>>

    @POST("groups/{gid}/leave")
    fun leaveGroup(@Path("gid") groupId: String, @Query("keepChallenges") keepChallenges: String): Flowable<HabitResponse<Void>>

    @POST("groups/{gid}/chat")
    fun postGroupChat(@Path("gid") groupId: String, @Body message: Map<String, String>): Flowable<HabitResponse<PostChatMessageResult>>

    @DELETE("groups/{gid}/chat/{messageId}")
    fun deleteMessage(@Path("gid") groupId: String, @Path("messageId") messageId: String): Flowable<HabitResponse<Void>>

    @DELETE("inbox/messages/{messageId}")
    fun deleteInboxMessage(@Path("messageId") messageId: String): Flowable<HabitResponse<Void>>

    @GET("groups/{gid}/members")
    fun getGroupMembers(
        @Path("gid") groupId: String,
        @Query("includeAllPublicFields") includeAllPublicFields: Boolean?
    ): Flowable<HabitResponse<List<Member>>>

    @GET("groups/{gid}/members")
    fun getGroupMembers(
        @Path("gid") groupId: String,
        @Query("includeAllPublicFields") includeAllPublicFields: Boolean?,
        @Query("lastId") lastId: String
    ): Flowable<HabitResponse<List<Member>>>

    // Like returns the full chat list
    @POST("groups/{gid}/chat/{mid}/like")
    fun likeMessage(@Path("gid") groupId: String, @Path("mid") mid: String): Flowable<HabitResponse<ChatMessage>>

    @POST("groups/{gid}/chat/{mid}/flag")
    fun flagMessage(
        @Path("gid") groupId: String,
        @Path("mid") mid: String,
        @Body data: Map<String, String>
    ): Flowable<HabitResponse<Void>>

    @POST("groups/{gid}/chat/seen")
    fun seenMessages(@Path("gid") groupId: String): Flowable<HabitResponse<Void>>

    @POST("groups/{gid}/invite")
    fun inviteToGroup(@Path("gid") groupId: String, @Body inviteData: Map<String, Any>): Flowable<HabitResponse<List<Void>>>

    @POST("groups/{gid}/reject-invite")
    fun rejectGroupInvite(@Path("gid") groupId: String): Flowable<HabitResponse<Void>>

    @POST("groups/{gid}/quests/accept")
    fun acceptQuest(@Path("gid") groupId: String): Flowable<HabitResponse<Void>>

    @POST("groups/{gid}/quests/reject")
    fun rejectQuest(@Path("gid") groupId: String): Flowable<HabitResponse<Void>>

    @POST("groups/{gid}/quests/cancel")
    fun cancelQuest(@Path("gid") groupId: String): Flowable<HabitResponse<Void>>

    @POST("groups/{gid}/quests/force-start")
    fun forceStartQuest(@Path("gid") groupId: String, @Body group: Group): Flowable<HabitResponse<Quest>>

    @POST("groups/{gid}/quests/invite/{questKey}")
    fun inviteToQuest(@Path("gid") groupId: String, @Path("questKey") questKey: String): Flowable<HabitResponse<Quest>>

    @POST("groups/{gid}/quests/abort")
    fun abortQuest(@Path("gid") groupId: String): Flowable<HabitResponse<Quest>>

    @POST("groups/{gid}/quests/leave")
    fun leaveQuest(@Path("gid") groupId: String): Flowable<HabitResponse<Void>>

    @POST("/iap/android/verify")
    fun validatePurchase(@Body request: PurchaseValidationRequest): Flowable<HabitResponse<PurchaseValidationResult>>

    @POST("/iap/android/subscribe")
    fun validateSubscription(@Body request: PurchaseValidationRequest): Flowable<HabitResponse<Void>>

    @GET("/iap/android/subscribe/cancel")
    fun cancelSubscription(): Flowable<HabitResponse<Void>>

    @POST("/iap/android/norenew-subscribe")
    fun validateNoRenewSubscription(@Body request: PurchaseValidationRequest): Flowable<HabitResponse<Void>>

    @POST("user/custom-day-start")
    fun changeCustomDayStart(@Body updateObject: Map<String, Any>): Flowable<HabitResponse<User>>

    // Members URL
    @GET("members/{mid}")
    fun getMember(@Path("mid") memberId: String): Flowable<HabitResponse<Member>>

    @GET("members/username/{username}")
    fun getMemberWithUsername(@Path("username") username: String): Flowable<HabitResponse<Member>>

    @GET("members/{mid}/achievements")
    fun getMemberAchievements(@Path("mid") memberId: String): Flowable<HabitResponse<List<Achievement>>>

    @POST("members/send-private-message")
    fun postPrivateMessage(@Body messageDetails: Map<String, String>): Flowable<HabitResponse<PostChatMessageResult>>

    @GET("members/find/{username}")
    fun findUsernames(
        @Path("username") username: String,
        @Query("context") context: String?,
        @Query("id") id: String?
    ): Flowable<HabitResponse<List<FindUsernameResult>>>

    @POST("members/flag-private-message/{mid}")
    fun flagInboxMessage(@Path("mid") mid: String, @Body data: Map<String, String>): Flowable<HabitResponse<Void>>

    @GET("shops/{identifier}")
    fun retrieveShopInventory(@Path("identifier") identifier: String): Flowable<HabitResponse<Shop>>

    @GET("shops/market-gear")
    fun retrieveMarketGear(): Flowable<HabitResponse<Shop>>

    // Push notifications
    @POST("user/push-devices")
    fun addPushDevice(@Body pushDeviceData: Map<String, String>): Flowable<HabitResponse<List<Void>>>

    @DELETE("user/push-devices/{regId}")
    fun deletePushDevice(@Path("regId") regId: String): Flowable<HabitResponse<List<Void>>>

    /* challenges api */

    @GET("challenges/user")
    fun getUserChallenges(@Query("page") page: Int?, @Query("member") memberOnly: Boolean): Flowable<HabitResponse<List<Challenge>>>

    @GET("challenges/user")
    fun getUserChallenges(@Query("page") page: Int?): Flowable<HabitResponse<List<Challenge>>>

    @GET("tasks/challenge/{challengeId}")
    fun getChallengeTasks(@Path("challengeId") challengeId: String): Flowable<HabitResponse<TaskList>>

    @GET("challenges/{challengeId}")
    fun getChallenge(@Path("challengeId") challengeId: String): Flowable<HabitResponse<Challenge>>

    @POST("challenges/{challengeId}/join")
    fun joinChallenge(@Path("challengeId") challengeId: String): Flowable<HabitResponse<Challenge>>

    @POST("challenges/{challengeId}/leave")
    fun leaveChallenge(@Path("challengeId") challengeId: String, @Body body: LeaveChallengeBody): Flowable<HabitResponse<Void>>

    @POST("challenges")
    fun createChallenge(@Body challenge: Challenge): Flowable<HabitResponse<Challenge>>

    @POST("tasks/challenge/{challengeId}")
    fun createChallengeTasks(@Path("challengeId") challengeId: String, @Body tasks: List<Task>): Flowable<HabitResponse<List<Task>>>

    @POST("tasks/challenge/{challengeId}")
    fun createChallengeTask(@Path("challengeId") challengeId: String, @Body task: Task): Flowable<HabitResponse<Task>>

    @PUT("challenges/{challengeId}")
    fun updateChallenge(@Path("challengeId") challengeId: String, @Body challenge: Challenge): Flowable<HabitResponse<Challenge>>

    @DELETE("challenges/{challengeId}")
    fun deleteChallenge(@Path("challengeId") challengeId: String): Flowable<HabitResponse<Void>>

    // DEBUG: These calls only work on a local development server

    @POST("debug/add-ten-gems")
    fun debugAddTenGems(): Flowable<HabitResponse<Void>>

    // Notifications
    @POST("notifications/{notificationId}/read")
    fun readNotification(@Path("notificationId") notificationId: String): Flowable<HabitResponse<List<Any>>>

    @POST("notifications/read")
    fun readNotifications(@Body notificationIds: Map<String, List<String>>): Flowable<HabitResponse<List<Any>>>

    @POST("notifications/see")
    fun seeNotifications(@Body notificationIds: Map<String, List<String>>): Flowable<HabitResponse<List<Any>>>

    @POST("user/open-mystery-item")
    fun openMysteryItem(): Flowable<HabitResponse<Equipment>>

    @POST("cron")
    fun runCron(): Flowable<HabitResponse<Void>>

    @POST("user/reset")
    fun resetAccount(): Flowable<HabitResponse<Void>>

    @HTTP(method = "DELETE", path = "user", hasBody = true)
    fun deleteAccount(@Body body: Map<String, String>): Flowable<HabitResponse<Void>>

    @GET("user/toggle-pinned-item/{pinType}/{path}")
    fun togglePinnedItem(@Path("pinType") pinType: String, @Path("path") path: String): Flowable<HabitResponse<Void>>

    @POST("user/reset-password")
    fun sendPasswordResetEmail(@Body data: Map<String, String>): Flowable<HabitResponse<Void>>

    @PUT("user/auth/update-username")
    fun updateLoginName(@Body data: Map<String, String>): Flowable<HabitResponse<Void>>

    @POST("user/auth/verify-username")
    fun verifyUsername(@Body data: Map<String, String>): Flowable<HabitResponse<VerifyUsernameResponse>>

    @PUT("user/auth/update-email")
    fun updateEmail(@Body data: Map<String, String>): Flowable<HabitResponse<Void>>

    @PUT("user/auth/update-password")
    fun updatePassword(@Body data: Map<String, String>): Flowable<HabitResponse<Void>>

    @POST("user/allocate")
    fun allocatePoint(@Query("stat") stat: String): Flowable<HabitResponse<Stats>>

    @POST("user/allocate-bulk")
    fun bulkAllocatePoints(@Body stats: Map<String, Map<String, Int>>): Flowable<HabitResponse<Stats>>

    @POST("members/transfer-gems")
    fun transferGems(@Body data: Map<String, Any>): Flowable<HabitResponse<Void>>

    @POST("tasks/unlink-all/{challengeID}")
    fun unlinkAllTasks(@Path("challengeID") challengeID: String?, @Query("keep") keepOption: String): Flowable<HabitResponse<Void>>

    @POST("user/block/{userID}")
    fun blockMember(@Path("userID") userID: String): Flowable<HabitResponse<List<String>>>

    @POST("user/reroll")
    fun reroll(): Flowable<HabitResponse<User>>

    // Team Plans

    @GET("group-plans")
    fun getTeamPlans(): Flowable<HabitResponse<List<TeamPlan>>>

    @GET("tasks/group/{groupID}")
    fun getTeamPlanTasks(@Path("groupID") groupId: String): Flowable<HabitResponse<TaskList>>
}
