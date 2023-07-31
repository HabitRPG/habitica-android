package com.habitrpg.android.habitica.data

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
import com.habitrpg.common.habitica.api.HostConfig
import com.habitrpg.common.habitica.models.PurchaseValidationRequest
import com.habitrpg.common.habitica.models.PurchaseValidationResult
import com.habitrpg.common.habitica.models.auth.UserAuthResponse
import com.habitrpg.shared.habitica.models.responses.ErrorResponse
import com.habitrpg.shared.habitica.models.responses.FeedResponse
import com.habitrpg.shared.habitica.models.responses.Status
import com.habitrpg.shared.habitica.models.responses.TaskDirectionData
import com.habitrpg.shared.habitica.models.responses.VerifyUsernameResponse
import retrofit2.HttpException

interface ApiClient {

    val hostConfig: HostConfig

    suspend fun getStatus(): Status?

    /* user API */

    suspend fun getTasks(): TaskList?

    /* challenges api */

    suspend fun getUserChallenges(page: Int, memberOnly: Boolean): List<Challenge>?

    suspend fun getWorldState(): WorldState?
    var languageCode: String?
    suspend fun getContent(language: String? = null): ContentResult?

    suspend fun updateUser(updateDictionary: Map<String, Any?>): User?

    suspend fun registrationLanguage(registrationLanguage: String): User?

    suspend fun retrieveInAppRewards(): List<ShopItem>?

    suspend fun equipItem(type: String, itemKey: String): Items?

    suspend fun buyItem(itemKey: String, purchaseQuantity: Int): BuyResponse?

    suspend fun purchaseItem(type: String, itemKey: String, purchaseQuantity: Int): Void?

    suspend fun purchaseHourglassItem(type: String, itemKey: String): Void?

    suspend fun purchaseMysterySet(itemKey: String): Void?

    suspend fun purchaseQuest(key: String): Void?
    suspend fun purchaseSpecialSpell(key: String): Void?
    suspend fun validateSubscription(request: PurchaseValidationRequest): Any?
    suspend fun validateNoRenewSubscription(request: PurchaseValidationRequest): Any?
    suspend fun cancelSubscription(): Void?

    suspend fun sellItem(itemType: String, itemKey: String): User?

    suspend fun feedPet(petKey: String, foodKey: String): FeedResponse?

    suspend fun hatchPet(eggKey: String, hatchingPotionKey: String): Items?
    suspend fun getTasks(type: String): TaskList?
    suspend fun getTasks(type: String, dueDate: String): TaskList?

    suspend fun unlockPath(path: String): UnlockResponse?

    suspend fun getTask(id: String): Task?

    suspend fun postTaskDirection(id: String, direction: String): TaskDirectionData?
    suspend fun bulkScoreTasks(data: List<Map<String, String>>): BulkTaskScoringData?

    suspend fun postTaskNewPosition(id: String, position: Int): List<String>?

    suspend fun scoreChecklistItem(taskId: String, itemId: String): Task?

    suspend fun createTask(item: Task): Task?
    suspend fun createGroupTask(groupId: String, item: Task): Task?

    suspend fun createTasks(tasks: List<Task>): List<Task>?

    suspend fun updateTask(id: String, item: Task): Task?

    suspend fun deleteTask(id: String): Void?

    suspend fun createTag(tag: Tag): Tag?

    suspend fun updateTag(id: String, tag: Tag): Tag?

    suspend fun deleteTag(id: String): Void?

    suspend fun registerUser(username: String, email: String, password: String, confirmPassword: String): UserAuthResponse?

    suspend fun connectUser(username: String, password: String): UserAuthResponse?

    suspend fun connectSocial(network: String, userId: String, accessToken: String): UserAuthResponse?
    suspend fun disconnectSocial(network: String): Void?

    suspend fun loginApple(authToken: String): UserAuthResponse?

    suspend fun sleep(): Boolean?
    suspend fun revive(): User?

    suspend fun useSkill(skillName: String, targetType: String, targetId: String): SkillResponse?

    suspend fun useSkill(skillName: String, targetType: String): SkillResponse?

    suspend fun changeClass(className: String?): User?

    suspend fun disableClasses(): User?

    suspend fun markPrivateMessagesRead()

    /* Group API */

    suspend fun listGroups(type: String): List<Group>?

    suspend fun getGroup(groupId: String): Group?

    suspend fun createGroup(group: Group): Group?
    suspend fun updateGroup(id: String, item: Group): Group?
    suspend fun removeMemberFromGroup(groupID: String, userID: String): Void?

    suspend fun listGroupChat(groupId: String): List<ChatMessage>?

    suspend fun joinGroup(groupId: String): Group?

    suspend fun leaveGroup(groupId: String, keepChallenges: String): Void?

    suspend fun postGroupChat(groupId: String, message: Map<String, String>): PostChatMessageResult?

    suspend fun deleteMessage(groupId: String, messageId: String): Void?
    suspend fun deleteInboxMessage(id: String): Void?

    suspend fun getGroupMembers(groupId: String, includeAllPublicFields: Boolean?): List<Member>?

    suspend fun getGroupMembers(groupId: String, includeAllPublicFields: Boolean?, lastId: String): List<Member>?

    // Like returns the full chat list
    suspend fun likeMessage(groupId: String, mid: String): ChatMessage?

    suspend fun flagMessage(groupId: String, mid: String, data: MutableMap<String, String>): Void?
    suspend fun flagInboxMessage(mid: String, data: MutableMap<String, String>): Void?

    suspend fun seenMessages(groupId: String): Void?

    suspend fun inviteToGroup(groupId: String, inviteData: Map<String, Any>): List<InviteResponse>?

    suspend fun rejectGroupInvite(groupId: String): Void?

    suspend fun acceptQuest(groupId: String): Void?

    suspend fun rejectQuest(groupId: String): Void?

    suspend fun cancelQuest(groupId: String): Void?

    suspend fun forceStartQuest(groupId: String, group: Group): Quest?

    suspend fun inviteToQuest(groupId: String, questKey: String): Quest?

    suspend fun abortQuest(groupId: String): Quest?

    suspend fun leaveQuest(groupId: String): Void?

    suspend fun validatePurchase(request: PurchaseValidationRequest): PurchaseValidationResult?

    suspend fun changeCustomDayStart(updateObject: Map<String, Any>): User?

    // Members URL
    suspend fun getMember(memberId: String): Member?
    suspend fun getMemberWithUsername(username: String): Member?

    suspend fun getMemberAchievements(memberId: String): List<Achievement>?

    suspend fun postPrivateMessage(messageDetails: Map<String, String>): PostChatMessageResult?

    suspend fun retrieveShopIventory(identifier: String): Shop?

    // Push notifications
    suspend fun addPushDevice(pushDeviceData: Map<String, String>): List<Void>?

    suspend fun deletePushDevice(regId: String): List<Void>?

    suspend fun getChallengeTasks(challengeId: String): TaskList?

    suspend fun getChallenge(challengeId: String): Challenge?

    suspend fun joinChallenge(challengeId: String): Challenge?

    suspend fun leaveChallenge(challengeId: String, body: LeaveChallengeBody): Void?

    suspend fun createChallenge(challenge: Challenge): Challenge?

    suspend fun createChallengeTasks(challengeId: String, tasks: List<Task>): List<Task>?
    suspend fun createChallengeTask(challengeId: String, task: Task): Task?
    suspend fun updateChallenge(challenge: Challenge): Challenge?
    suspend fun deleteChallenge(challengeId: String): Void?

    // DEBUG: These calls only work on a local development server

    suspend fun debugAddTenGems(): Void?

    suspend fun getNews(): List<Any>?

    // Notifications
    suspend fun readNotification(notificationId: String): List<Any>?
    suspend fun readNotifications(notificationIds: Map<String, List<String>>): List<Any>?
    suspend fun seeNotifications(notificationIds: Map<String, List<String>>): List<Any>?

    fun getErrorResponse(throwable: HttpException): ErrorResponse

    fun updateAuthenticationCredentials(userID: String?, apiToken: String?)

    fun hasAuthenticationKeys(): Boolean

    suspend fun retrieveUser(withTasks: Boolean = false): User?
    suspend fun retrieveInboxMessages(uuid: String, page: Int): List<ChatMessage>?
    suspend fun retrieveInboxConversations(): List<InboxConversation>?

    suspend fun openMysteryItem(): Equipment?

    suspend fun runCron(): Void?

    suspend fun reroll(): User?

    suspend fun resetAccount(): Void?
    suspend fun deleteAccount(password: String): Void?

    suspend fun togglePinnedItem(pinType: String, path: String): Void?

    suspend fun sendPasswordResetEmail(email: String): Void?

    suspend fun updateLoginName(newLoginName: String, password: String): Void?
    suspend fun updateUsername(newLoginName: String): Void?

    suspend fun updateEmail(newEmail: String, password: String): Void?

    suspend fun updatePassword(oldPassword: String, newPassword: String, newPasswordConfirmation: String): Void?

    suspend fun allocatePoint(stat: String): Stats?

    suspend fun bulkAllocatePoints(strength: Int, intelligence: Int, constitution: Int, perception: Int): Stats?

    suspend fun retrieveMarketGear(): Shop?
    suspend fun verifyUsername(username: String): VerifyUsernameResponse?
    fun updateServerUrl(newAddress: String?)
    suspend fun findUsernames(username: String, context: String?, id: String?): List<FindUsernameResult>?

    suspend fun transferGems(giftedID: String, amount: Int): Void?
    suspend fun unlinkAllTasks(challengeID: String?, keepOption: String): Void?
    suspend fun blockMember(userID: String): List<String>?
    suspend fun getTeamPlans(): List<TeamPlan>?
    suspend fun getTeamPlanTasks(teamID: String): TaskList?
    suspend fun assignToTask(taskId: String, ids: List<String>): Task?
    suspend fun unassignFromTask(taskId: String, userID: String): Task?
    suspend fun updateMember(memberID: String, updateData: Map<String, Any?>): Member?
    suspend fun getHallMember(userId: String): Member?
    suspend fun markTaskNeedsWork(taskID: String, userID: String): Task?
    suspend fun retrievePartySeekingUsers(page: Int): List<Member>?
    suspend fun getGroupInvites(groupId: String, includeAllPublicFields: Boolean?): List<Member>?
}
