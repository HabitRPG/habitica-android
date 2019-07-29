package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.api.HostConfig
import com.habitrpg.android.habitica.models.*
import com.habitrpg.android.habitica.models.auth.UserAuthResponse
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.responses.*
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.FindUsernameResult
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.models.user.Items
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import retrofit2.HttpException


interface ApiClient {

    val hostConfig: HostConfig

    val status: Flowable<Status>

    val content: Flowable<ContentResult>

    /* user API */

    val user: Flowable<User>


    val tasks: Flowable<TaskList>

    /* challenges api */

    fun getUserChallenges(page: Int, memberOnly: Boolean): Flowable<List<Challenge>>

    val worldState: Flowable<WorldState>
    fun setLanguageCode(languageCode: String)
    fun getContent(language: String): Flowable<ContentResult>

    fun updateUser(updateDictionary: Map<String, Any>): Flowable<User>

    fun registrationLanguage(registrationLanguage: String): Flowable<User>

    fun retrieveInAppRewards(): Flowable<List<ShopItem>>
    fun retrieveOldGear(): Flowable<List<ShopItem>>

    fun equipItem(type: String, itemKey: String): Flowable<Items>

    fun buyItem(itemKey: String): Flowable<BuyResponse>

    fun purchaseItem(type: String, itemKey: String): Flowable<Any>

    fun purchaseHourglassItem(type: String, itemKey: String): Flowable<Any>

    fun purchaseMysterySet(itemKey: String): Flowable<Any>

    fun purchaseQuest(key: String): Flowable<Any>
    fun validateSubscription(request: SubscriptionValidationRequest): Flowable<Any>
    fun validateNoRenewSubscription(request: PurchaseValidationRequest): Flowable<Any>

    fun sellItem(itemType: String, itemKey: String): Flowable<User>

    fun feedPet(petKey: String, foodKey: String): Flowable<FeedResponse>

    fun hatchPet(eggKey: String, hatchingPotionKey: String): Flowable<Items>
    fun getTasks(type: String): Flowable<TaskList>
    fun getTasks(type: String, dueDate: String): Flowable<TaskList>


    fun unlockPath(path: String): Flowable<UnlockResponse>

    fun getTask(id: String): Flowable<Task>

    fun postTaskDirection(id: String, direction: String): Flowable<TaskDirectionData>

    fun postTaskNewPosition(id: String, position: Int): Flowable<List<String>>

    fun scoreChecklistItem(taskId: String, itemId: String): Flowable<Task>

    fun createTask(item: Task): Flowable<Task>

    fun createTasks(tasks: List<Task>): Flowable<List<Task>>

    fun updateTask(id: String, item: Task): Flowable<Task>

    fun deleteTask(id: String): Flowable<Void>


    fun createTag(tag: Tag): Flowable<Tag>

    fun updateTag(id: String, tag: Tag): Flowable<Tag>

    fun deleteTag(id: String): Flowable<Void>

    fun registerUser(username: String, email: String, password: String, confirmPassword: String): Flowable<UserAuthResponse>

    fun connectUser(username: String, password: String): Flowable<UserAuthResponse>

    fun connectSocial(network: String, userId: String, accessToken: String): Flowable<UserAuthResponse>
    fun sleep(): Flowable<Boolean>

    fun revive(): Flowable<User>

    fun useSkill(skillName: String, targetType: String, targetId: String): Flowable<SkillResponse>

    fun useSkill(skillName: String, targetType: String): Flowable<SkillResponse>

    fun changeClass(): Flowable<User>

    fun changeClass(className: String): Flowable<User>

    fun disableClasses(): Flowable<User>

    fun markPrivateMessagesRead(): Flowable<Void>

    /* Group API */

    fun listGroups(type: String): Flowable<List<Group>>

    fun getGroup(groupId: String): Flowable<Group>

    fun createGroup(group: Group): Flowable<Group>
    fun updateGroup(id: String, item: Group): Flowable<Void>

    fun listGroupChat(groupId: String): Flowable<List<ChatMessage>>

    fun joinGroup(groupId: String): Flowable<Group>

    fun leaveGroup(groupId: String): Flowable<Void>

    fun postGroupChat(groupId: String, message: Map<String, String>): Flowable<PostChatMessageResult>

    fun deleteMessage(groupId: String, messageId: String): Flowable<Void>
    fun deleteInboxMessage(id: String): Flowable<Void>

    fun getGroupMembers(groupId: String, includeAllPublicFields: Boolean?): Flowable<List<Member>>

    fun getGroupMembers(groupId: String, includeAllPublicFields: Boolean?, lastId: String): Flowable<List<Member>>

    // Like returns the full chat list
    fun likeMessage(groupId: String, mid: String): Flowable<ChatMessage>

    fun flagMessage(groupId: String, mid: String, data: MutableMap<String, String>): Flowable<Void>

    fun seenMessages(groupId: String): Flowable<Void>

    fun inviteToGroup(groupId: String, inviteData: Map<String, Any>): Flowable<List<String>>

    fun rejectGroupInvite(groupId: String): Flowable<Void>

    fun acceptQuest(groupId: String): Flowable<Void>

    fun rejectQuest(groupId: String): Flowable<Void>

    fun cancelQuest(groupId: String): Flowable<Void>

    fun forceStartQuest(groupId: String, group: Group): Flowable<Quest>

    fun inviteToQuest(groupId: String, questKey: String): Flowable<Quest>

    fun abortQuest(groupId: String): Flowable<Quest>

    fun leaveQuest(groupId: String): Flowable<Void>

    fun validatePurchase(request: PurchaseValidationRequest): Flowable<PurchaseValidationResult>

    fun changeCustomDayStart(updateObject: Map<String, Any>): Flowable<User>

    //Members URL
    fun getMember(memberId: String): Flowable<Member>
    fun getMemberWithUsername(username: String): Flowable<Member>

    fun getMemberAchievements(memberId: String): Flowable<List<Achievement>>

    fun postPrivateMessage(messageDetails: Map<String, String>): Flowable<PostChatMessageResult>

    fun retrieveShopIventory(identifier: String): Flowable<Shop>

    //Push notifications
    fun addPushDevice(pushDeviceData: Map<String, String>): Flowable<List<Void>>

    fun deletePushDevice(regId: String): Flowable<List<Void>>

    fun getChallengeTasks(challengeId: String): Flowable<TaskList>

    fun getChallenge(challengeId: String): Flowable<Challenge>

    fun joinChallenge(challengeId: String): Flowable<Challenge>

    fun leaveChallenge(challengeId: String, body: LeaveChallengeBody): Flowable<Void>


    fun createChallenge(challenge: Challenge): Flowable<Challenge>

    fun createChallengeTasks(challengeId: String, tasks: List<Task>): Flowable<List<Task>>
    fun createChallengeTask(challengeId: String, task: Task): Flowable<Task>
    fun updateChallenge(challenge: Challenge): Flowable<Challenge>
    fun deleteChallenge(challengeId: String): Flowable<Void>

    //DEBUG: These calls only work on a local development server

    fun debugAddTenGems(): Flowable<Void>

    // Notifications
    fun readNotification(notificationId: String): Flowable<List<*>>
    fun readNotifications(notificationIds: Map<String, List<String>>): Flowable<List<*>>
    fun seeNotifications(notificationIds: Map<String, List<String>>): Flowable<List<*>>

    fun getErrorResponse(throwable: HttpException): ErrorResponse

    fun updateAuthenticationCredentials(userID: String?, apiToken: String?)

    fun hasAuthenticationKeys(): Boolean

    fun retrieveUser(withTasks: Boolean): Flowable<User>
    fun retrieveInboxMessages(): Flowable<List<ChatMessage>>

    fun <T> configureApiCallObserver(): FlowableTransformer<HabitResponse<T>, T>

    fun openMysteryItem(): Flowable<Equipment>

    fun runCron(): Flowable<Void>

    fun resetAccount(): Flowable<Void>
    fun deleteAccount(password: String): Flowable<Void>

    fun togglePinnedItem(pinType: String, path: String): Flowable<Void>

    fun sendPasswordResetEmail(email: String): Flowable<Void>

    fun updateLoginName(newLoginName: String, password: String): Flowable<Void>
    fun updateUsername(newLoginName: String): Flowable<Void>

    fun updateEmail(newEmail: String, password: String): Flowable<Void>

    fun updatePassword(oldPassword: String, newPassword: String, newPasswordConfirmation: String): Flowable<Void>

    fun allocatePoint(stat: String): Flowable<Stats>

    fun bulkAllocatePoints(strength: Int, intelligence: Int, constitution: Int, perception: Int): Flowable<Stats>

    fun retrieveMarketGear(): Flowable<Shop>
    fun verifyUsername(username: String): Flowable<VerifyUsernameResponse>
    fun updateServerUrl(newAddress: String?)
    fun findUsernames(username: String, context: String?, id: String?): Flowable<List<FindUsernameResult>>
}
