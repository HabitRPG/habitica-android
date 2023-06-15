package com.habitrpg.android.habitica.api

import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.LeaveChallengeBody
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.TeamPlan
import com.habitrpg.android.habitica.models.WorldState
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.invitations.InviteResponse
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.responses.BulkTaskScoringData
import com.habitrpg.android.habitica.models.responses.BuyResponse
import com.habitrpg.android.habitica.models.responses.PostChatMessageResult
import com.habitrpg.android.habitica.models.responses.SkillResponse
import com.habitrpg.android.habitica.models.responses.UnlockResponse
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
import com.habitrpg.common.habitica.models.HabitResponse
import com.habitrpg.common.habitica.models.PurchaseValidationRequest
import com.habitrpg.common.habitica.models.PurchaseValidationResult
import com.habitrpg.common.habitica.models.auth.UserAuth
import com.habitrpg.common.habitica.models.auth.UserAuthResponse
import com.habitrpg.common.habitica.models.auth.UserAuthSocial
import com.habitrpg.shared.habitica.models.responses.FeedResponse
import com.habitrpg.shared.habitica.models.responses.Status
import com.habitrpg.shared.habitica.models.responses.TaskDirectionData
import com.habitrpg.shared.habitica.models.responses.VerifyUsernameResponse
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
    @GET("status")
    suspend fun getStatus(): HabitResponse<Status>

    /* user API */

    @GET("user/")
    suspend fun getUser(): HabitResponse<User>

    @GET("inbox/messages")
    suspend fun getInboxMessages(@Query("conversation") uuid: String, @Query("page") page: Int): HabitResponse<List<ChatMessage>>
    @GET("inbox/conversations")
    suspend fun getInboxConversations(): HabitResponse<List<InboxConversation>>

    @GET("tasks/user")
    suspend fun getTasks(): HabitResponse<TaskList>

    @GET("world-state")
    suspend fun worldState(): HabitResponse<WorldState>

    @GET("content")
    suspend fun getContent(@Query("language") language: String?): HabitResponse<ContentResult>

    @PUT("user/")
    suspend fun updateUser(@Body updateDictionary: Map<String, Any?>): HabitResponse<User>

    @PUT("user/")
    suspend fun registrationLanguage(@Header("Accept-Language") registrationLanguage: String): HabitResponse<User>

    @GET("user/in-app-rewards")
    suspend fun retrieveInAppRewards(): HabitResponse<List<ShopItem>>

    @POST("user/equip/{type}/{key}")
    suspend fun equipItem(@Path("type") type: String, @Path("key") itemKey: String): HabitResponse<Items>

    @POST("user/buy/{key}")
    suspend fun buyItem(@Path("key") itemKey: String, @Body quantity: Map<String, Int>): HabitResponse<BuyResponse>

    @POST("user/purchase/{type}/{key}")
    suspend fun purchaseItem(
        @Path("type") type: String,
        @Path("key") itemKey: String,
        @Body quantity: Map<String, Int>
    ): HabitResponse<Void>

    @POST("user/purchase-hourglass/{type}/{key}")
    suspend fun purchaseHourglassItem(@Path("type") type: String, @Path("key") itemKey: String): HabitResponse<Void>

    @POST("user/buy-mystery-set/{key}")
    suspend fun purchaseMysterySet(@Path("key") itemKey: String): HabitResponse<Void>

    @POST("user/buy-quest/{key}")
    suspend fun purchaseQuest(@Path("key") key: String): HabitResponse<Void>

    @POST("user/buy-special-spell/{key}")
    suspend fun purchaseSpecialSpell(@Path("key") key: String): HabitResponse<Void>

    @POST("user/sell/{type}/{key}")
    suspend fun sellItem(@Path("type") itemType: String, @Path("key") itemKey: String): HabitResponse<User>

    @POST("user/feed/{pet}/{food}")
    suspend fun feedPet(@Path("pet") petKey: String, @Path("food") foodKey: String): HabitResponse<FeedResponse>

    @POST("user/hatch/{egg}/{hatchingPotion}")
    suspend fun hatchPet(@Path("egg") eggKey: String, @Path("hatchingPotion") hatchingPotionKey: String): HabitResponse<Items>

    @GET("tasks/user")
    suspend fun getTasks(@Query("type") type: String): HabitResponse<TaskList>

    @GET("tasks/user")
    suspend fun getTasks(@Query("type") type: String, @Query("dueDate") dueDate: String): HabitResponse<TaskList>

    @POST("user/unlock")
    suspend fun unlockPath(@Query("path") path: String): HabitResponse<UnlockResponse>

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
    @POST("tasks/group/{groupId}")
    suspend fun createGroupTask(@Path("groupId") groupId: String, @Body item: Task): HabitResponse<Task>

    @POST("tasks/user")
    suspend fun createTasks(@Body tasks: List<Task>): HabitResponse<List<Task>>

    @PUT("tasks/{id}")
    suspend fun updateTask(@Path("id") id: String, @Body item: Task): HabitResponse<Task>

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String): HabitResponse<Void>

    @POST("tags")
    suspend fun createTag(@Body tag: Tag): HabitResponse<Tag>

    @PUT("tags/{id}")
    suspend fun updateTag(@Path("id") id: String, @Body tag: Tag): HabitResponse<Tag>

    @DELETE("tags/{id}")
    suspend fun deleteTag(@Path("id") id: String): HabitResponse<Void>

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

    @POST("user/class/cast/{skill}")
    suspend fun useSkill(
        @Path("skill") skillName: String,
        @Query("targetType") targetType: String,
        @Query("targetId") targetId: String
    ): HabitResponse<SkillResponse>

    @POST("user/class/cast/{skill}")
    suspend fun useSkill(@Path("skill") skillName: String, @Query("targetType") targetType: String): HabitResponse<SkillResponse>

    @POST("user/change-class")
    suspend fun changeClass(): HabitResponse<User>

    @POST("user/change-class")
    suspend fun changeClass(@Query("class") className: String): HabitResponse<User>

    @POST("user/disable-classes")
    suspend fun disableClasses(): HabitResponse<User>

    @POST("user/mark-pms-read")
    suspend fun markPrivateMessagesRead(): Void?

    /* Group API */

    @GET("groups")
    suspend fun listGroups(@Query("type") type: String): HabitResponse<List<Group>>

    @GET("groups/{gid}")
    suspend fun getGroup(@Path("gid") groupId: String): HabitResponse<Group>

    @POST("groups")
    suspend fun createGroup(@Body item: Group): HabitResponse<Group>

    @PUT("groups/{id}")
    suspend fun updateGroup(@Path("id") id: String, @Body item: Group): HabitResponse<Group>

    @POST("groups/{groupID}/removeMember/{userID}")
    suspend fun removeMemberFromGroup(@Path("groupID") groupID: String, @Path("userID") userID: String): HabitResponse<Void>

    @GET("groups/{gid}/chat")
    suspend fun listGroupChat(@Path("gid") groupId: String): HabitResponse<List<ChatMessage>>

    @POST("groups/{gid}/join")
    suspend fun joinGroup(@Path("gid") groupId: String): HabitResponse<Group>

    @POST("groups/{gid}/leave")
    suspend fun leaveGroup(@Path("gid") groupId: String, @Query("keepChallenges") keepChallenges: String): HabitResponse<Void>

    @POST("groups/{gid}/chat")
    suspend fun postGroupChat(@Path("gid") groupId: String, @Body message: Map<String, String>): HabitResponse<PostChatMessageResult>

    @DELETE("groups/{gid}/chat/{messageId}")
    suspend fun deleteMessage(@Path("gid") groupId: String, @Path("messageId") messageId: String): HabitResponse<Void>

    @DELETE("inbox/messages/{messageId}")
    suspend fun deleteInboxMessage(@Path("messageId") messageId: String): HabitResponse<Void>

    @GET("groups/{gid}/members")
    suspend fun getGroupMembers(
        @Path("gid") groupId: String,
        @Query("includeAllPublicFields") includeAllPublicFields: Boolean?
    ): HabitResponse<List<Member>>

    @GET("groups/{gid}/members")
    suspend fun getGroupMembers(
        @Path("gid") groupId: String,
        @Query("includeAllPublicFields") includeAllPublicFields: Boolean?,
        @Query("lastId") lastId: String
    ): HabitResponse<List<Member>>

    // Like returns the full chat list
    @POST("groups/{gid}/chat/{mid}/like")
    suspend fun likeMessage(@Path("gid") groupId: String, @Path("mid") mid: String): HabitResponse<ChatMessage>

    @POST("groups/{gid}/chat/{mid}/flag")
    suspend fun flagMessage(
        @Path("gid") groupId: String,
        @Path("mid") mid: String,
        @Body data: Map<String, String>
    ): HabitResponse<Void>

    @POST("groups/{gid}/chat/seen")
    suspend fun seenMessages(@Path("gid") groupId: String): HabitResponse<Void>

    @POST("groups/{gid}/invite")
    suspend fun inviteToGroup(@Path("gid") groupId: String, @Body inviteData: Map<String, Any>): HabitResponse<List<InviteResponse>>

    @POST("groups/{gid}/reject-invite")
    suspend fun rejectGroupInvite(@Path("gid") groupId: String): HabitResponse<Void>

    @POST("groups/{gid}/quests/accept")
    suspend fun acceptQuest(@Path("gid") groupId: String): HabitResponse<Void>

    @POST("groups/{gid}/quests/reject")
    suspend fun rejectQuest(@Path("gid") groupId: String): HabitResponse<Void>

    @POST("groups/{gid}/quests/cancel")
    suspend fun cancelQuest(@Path("gid") groupId: String): HabitResponse<Void>

    @POST("groups/{gid}/quests/force-start")
    suspend fun forceStartQuest(@Path("gid") groupId: String, @Body group: Group): HabitResponse<Quest>

    @POST("groups/{gid}/quests/invite/{questKey}")
    suspend fun inviteToQuest(@Path("gid") groupId: String, @Path("questKey") questKey: String): HabitResponse<Quest>

    @GET("groups/{gid}/invites")
    suspend fun getGroupInvites(@Path("gid") groupId: String,
        @Query("includeAllPublicFields") includeAllPublicFields: Boolean?): HabitResponse<List<Member>>

    @POST("groups/{gid}/quests/abort")
    suspend fun abortQuest(@Path("gid") groupId: String): HabitResponse<Quest>

    @POST("groups/{gid}/quests/leave")
    suspend fun leaveQuest(@Path("gid") groupId: String): HabitResponse<Void>

    @POST("/iap/android/verify")
    suspend fun validatePurchase(@Body request: PurchaseValidationRequest): HabitResponse<PurchaseValidationResult>

    @POST("/iap/android/subscribe")
    suspend fun validateSubscription(@Body request: PurchaseValidationRequest): HabitResponse<Void>

    @GET("/iap/android/subscribe/cancel")
    suspend fun cancelSubscription(): HabitResponse<Void>

    @POST("/iap/android/norenew-subscribe")
    suspend fun validateNoRenewSubscription(@Body request: PurchaseValidationRequest): HabitResponse<Void>

    @POST("user/custom-day-start")
    suspend fun changeCustomDayStart(@Body updateObject: Map<String, Any>): HabitResponse<User>

    // Members URL
    @GET("members/{mid}")
    suspend fun getMember(@Path("mid") memberId: String): HabitResponse<Member>

    @GET("members/username/{username}")
    suspend fun getMemberWithUsername(@Path("username") username: String): HabitResponse<Member>

    @GET("members/{mid}/achievements")
    suspend fun getMemberAchievements(@Path("mid") memberId: String, @Query("lang") language: String?): HabitResponse<List<Achievement>>

    @POST("members/send-private-message")
    suspend fun postPrivateMessage(@Body messageDetails: Map<String, String>): HabitResponse<PostChatMessageResult>

    @GET("members/find/{username}")
    suspend fun findUsernames(
        @Path("username") username: String,
        @Query("context") context: String?,
        @Query("id") id: String?
    ): HabitResponse<List<FindUsernameResult>>

    @POST("members/flag-private-message/{mid}")
    suspend fun flagInboxMessage(@Path("mid") mid: String, @Body data: Map<String, String>): HabitResponse<Void>

    @GET("shops/{identifier}")
    suspend fun retrieveShopInventory(@Path("identifier") identifier: String, @Query("lang") language: String?): HabitResponse<Shop>

    @GET("shops/market-gear")
    suspend fun retrieveMarketGear(@Query("lang") language: String?): HabitResponse<Shop>

    // Push notifications
    @POST("user/push-devices")
    suspend fun addPushDevice(@Body pushDeviceData: Map<String, String>): HabitResponse<List<Void>>

    @DELETE("user/push-devices/{regId}")
    suspend fun deletePushDevice(@Path("regId") regId: String): HabitResponse<List<Void>>

    /* challenges api */

    @GET("challenges/user")
    suspend fun getUserChallenges(@Query("page") page: Int?, @Query("member") memberOnly: Boolean): HabitResponse<List<Challenge>>

    @GET("challenges/user")
    suspend fun getUserChallenges(@Query("page") page: Int?): HabitResponse<List<Challenge>>

    @GET("tasks/challenge/{challengeId}")
    suspend fun getChallengeTasks(@Path("challengeId") challengeId: String): HabitResponse<TaskList>

    @GET("challenges/{challengeId}")
    suspend fun getChallenge(@Path("challengeId") challengeId: String): HabitResponse<Challenge>

    @POST("challenges/{challengeId}/join")
    suspend fun joinChallenge(@Path("challengeId") challengeId: String): HabitResponse<Challenge>

    @POST("challenges/{challengeId}/leave")
    suspend fun leaveChallenge(@Path("challengeId") challengeId: String, @Body body: LeaveChallengeBody): HabitResponse<Void>

    @POST("challenges")
    suspend fun createChallenge(@Body challenge: Challenge): HabitResponse<Challenge>

    @POST("tasks/challenge/{challengeId}")
    suspend fun createChallengeTasks(@Path("challengeId") challengeId: String, @Body tasks: List<Task>): HabitResponse<List<Task>>

    @POST("tasks/challenge/{challengeId}")
    suspend fun createChallengeTask(@Path("challengeId") challengeId: String, @Body task: Task): HabitResponse<Task>

    @PUT("challenges/{challengeId}")
    suspend fun updateChallenge(@Path("challengeId") challengeId: String, @Body challenge: Challenge): HabitResponse<Challenge>

    @DELETE("challenges/{challengeId}")
    suspend fun deleteChallenge(@Path("challengeId") challengeId: String): HabitResponse<Void>

    // DEBUG: These calls only work on a local development server

    @POST("debug/add-ten-gems")
    suspend fun debugAddTenGems(): HabitResponse<Void>

    // Notifications
    @POST("notifications/{notificationId}/read")
    suspend fun readNotification(@Path("notificationId") notificationId: String): HabitResponse<List<Any>>

    @POST("notifications/read")
    suspend fun readNotifications(@Body notificationIds: Map<String, List<String>>): HabitResponse<List<Any>>

    @POST("notifications/see")
    suspend fun seeNotifications(@Body notificationIds: Map<String, List<String>>): HabitResponse<List<Any>>

    @POST("user/open-mystery-item")
    suspend fun openMysteryItem(): HabitResponse<Equipment>

    @POST("cron")
    suspend fun runCron(): HabitResponse<Void>

    @POST("user/reset")
    suspend fun resetAccount(): HabitResponse<Void>

    @HTTP(method = "DELETE", path = "user", hasBody = true)
    suspend fun deleteAccount(@Body body: Map<String, String>): HabitResponse<Void>

    @GET("user/toggle-pinned-item/{pinType}/{path}")
    suspend fun togglePinnedItem(@Path("pinType") pinType: String, @Path("path") path: String): HabitResponse<Void>

    @POST("user/reset-password")
    suspend fun sendPasswordResetEmail(@Body data: Map<String, String>): HabitResponse<Void>

    @PUT("user/auth/update-username")
    suspend fun updateLoginName(@Body data: Map<String, String>): HabitResponse<Void>

    @POST("user/auth/verify-username")
    suspend fun verifyUsername(@Body data: Map<String, String>): HabitResponse<VerifyUsernameResponse>

    @PUT("user/auth/update-email")
    suspend fun updateEmail(@Body data: Map<String, String>): HabitResponse<Void>

    @PUT("user/auth/update-password")
    suspend fun updatePassword(@Body data: Map<String, String>): HabitResponse<Void>

    @POST("user/allocate")
    suspend fun allocatePoint(@Query("stat") stat: String): HabitResponse<Stats>

    @POST("user/allocate-bulk")
    suspend fun bulkAllocatePoints(@Body stats: Map<String, Map<String, Int>>): HabitResponse<Stats>

    @POST("members/transfer-gems")
    suspend fun transferGems(@Body data: Map<String, Any>): HabitResponse<Void>

    @POST("tasks/unlink-all/{challengeID}")
    suspend fun unlinkAllTasks(@Path("challengeID") challengeID: String?, @Query("keep") keepOption: String): HabitResponse<Void>

    @POST("user/block/{userID}")
    suspend fun blockMember(@Path("userID") userID: String): HabitResponse<List<String>>

    @POST("user/reroll")
    suspend fun reroll(): HabitResponse<User>

    // Team Plans

    @GET("group-plans")
    suspend fun getTeamPlans(): HabitResponse<List<TeamPlan>>

    @GET("tasks/group/{groupID}")
    suspend fun getTeamPlanTasks(@Path("groupID") groupId: String): HabitResponse<TaskList>

    @POST("tasks/{taskID}/assign")
    suspend fun assignToTask(@Path("taskID") taskId: String?, @Body ids: List<String>): HabitResponse<Task>

    @POST("tasks/{taskID}/unassign/{userID}")
    suspend fun unassignFromTask(@Path("taskID") taskID: String, @Path("userID") userID: String): HabitResponse<Task>

    @PUT("hall/heroes/{heroId}")
    suspend fun updateUser(@Path("heroId") memberID: String, @Body updateData: Map<String, Any?>): HabitResponse<Member>

    @GET("hall/heroes/{memberID}")
    suspend fun getHallMember(@Path("memberID") memberID: String): HabitResponse<Member>

    @POST("tasks/{taskID}/needs-work/{userID}")
    suspend fun markTaskNeedsWork(@Path("taskID") taskID: String, @Path("userID") userID: String): HabitResponse<Task>

    @GET("looking-for-party")
    suspend fun retrievePartySeekingUsers(@Query("page") page: Int): HabitResponse<List<Member>>
}
