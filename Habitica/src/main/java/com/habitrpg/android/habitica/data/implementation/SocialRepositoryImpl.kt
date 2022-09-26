package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.local.SocialLocalRepository
import com.habitrpg.android.habitica.helpers.ExceptionHandler
import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.responses.PostChatMessageResult
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.FindUsernameResult
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.social.GroupMembership
import com.habitrpg.android.habitica.models.social.InboxConversation
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.rxjava3.core.Flowable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class SocialRepositoryImpl(
    localRepository: SocialLocalRepository,
    apiClient: ApiClient,
    userID: String
) : BaseRepositoryImpl<SocialLocalRepository>(localRepository, apiClient, userID), SocialRepository {
    override suspend fun transferGroupOwnership(groupID: String, userID: String): Group? {
        val group = localRepository.getGroup(groupID).first()?.let { localRepository.getUnmanagedCopy(it) }
        group?.leaderID = userID
        return group?.let { apiClient.updateGroup(groupID, it) }
    }

    override suspend fun removeMemberFromGroup(groupID: String, userID: String): List<Member>? {
        apiClient.removeMemberFromGroup(groupID, userID)
        return retrieveGroupMembers(groupID, true)
    }

    override fun blockMember(userID: String): Flowable<List<String>> {
        return apiClient.blockMember(userID)
    }

    override fun getGroupMembership(id: String) = localRepository.getGroupMembership(userID, id)

    override fun getGroupMemberships(): Flowable<out List<GroupMembership>> {
        return localRepository.getGroupMemberships(userID)
    }

    override suspend fun retrieveGroupChat(groupId: String): List<ChatMessage>? {
        val messages = apiClient.listGroupChat(groupId)
        messages?.forEach { it.groupId = groupId }
        return messages
    }

    override fun getGroupChat(groupId: String): Flowable<out List<ChatMessage>> {
        return localRepository.getGroupChat(groupId)
    }

    override fun markMessagesSeen(seenGroupId: String) {
        apiClient.seenMessages(seenGroupId).subscribe({ }, ExceptionHandler.rx())
    }

    override fun flagMessage(chatMessageID: String, additionalInfo: String, groupID: String?): Flowable<Void> {
        return when {
            chatMessageID.isBlank() -> Flowable.empty()
            userID == BuildConfig.ANDROID_TESTING_UUID -> Flowable.empty()
            else -> {
                val data = mutableMapOf<String, String>()
                data["comment"] = additionalInfo
                if (groupID?.isNotBlank() != true) {
                    apiClient.flagInboxMessage(chatMessageID, data)
                } else {
                    apiClient.flagMessage(groupID, chatMessageID, data)
                }
            }
        }
    }

    override fun likeMessage(chatMessage: ChatMessage): Flowable<ChatMessage> {
        if (chatMessage.id.isBlank()) {
            return Flowable.empty()
        }
        val liked = chatMessage.userLikesMessage(userID)
        if (chatMessage.isManaged) {
            localRepository.likeMessage(chatMessage, userID, !liked)
        }
        return apiClient.likeMessage(chatMessage.groupId ?: "", chatMessage.id)
            .map {
                it.groupId = chatMessage.groupId
                it
            }
    }

    override fun deleteMessage(chatMessage: ChatMessage): Flowable<Void> {
        return if (chatMessage.isInboxMessage) {
            apiClient.deleteInboxMessage(chatMessage.id)
        } else {
            apiClient.deleteMessage(chatMessage.groupId ?: "", chatMessage.id)
        }.doOnNext { localRepository.deleteMessage(chatMessage.id) }
    }

    override fun postGroupChat(groupId: String, messageObject: HashMap<String, String>): Flowable<PostChatMessageResult> {
        return apiClient.postGroupChat(groupId, messageObject)
            .map { postChatMessageResult ->
                postChatMessageResult.message.groupId = groupId
                postChatMessageResult
            }
    }

    override fun postGroupChat(groupId: String, message: String): Flowable<PostChatMessageResult> {
        val messageObject = HashMap<String, String>()
        messageObject["message"] = message
        return postGroupChat(groupId, messageObject)
    }

    override suspend fun retrieveGroup(id: String): Group? {
        val group = apiClient.getGroup(id)
        group?.let { localRepository.saveGroup(it) }
        retrieveGroupChat(id)
        return group
    }

    override fun getGroup(id: String?): Flow<Group?> {
        if (id?.isNotBlank() != true) {
            return emptyFlow()
        }
        return localRepository.getGroup(id)
    }

    override suspend fun leaveGroup(id: String?, keepChallenges: Boolean): Group? {
        if (id?.isNotBlank() != true) {
            return null
        }

        apiClient.leaveGroup(id, if (keepChallenges) "remain-in-challenges" else "leave-challenges")
        localRepository.updateMembership(userID, id, false)
        return localRepository.getGroup(id).firstOrNull()
    }

    override suspend fun joinGroup(id: String?): Group? {
        if (id?.isNotBlank() != true) {
            return null
        }
        val group = apiClient.joinGroup(id)
        group?.let {
            localRepository.updateMembership(userID, id, true)
            localRepository.save(group)
        }
        return group
    }

    override suspend fun createGroup(
        name: String?,
        description: String?,
        leader: String?,
        type: String?,
        privacy: String?,
        leaderCreateChallenge: Boolean?
    ): Group? {
        val group = Group()
        group.name = name
        group.description = description
        group.type = type
        group.leaderID = leader
        group.privacy = privacy
        val savedGroup = apiClient.createGroup(group)
        savedGroup?.let { localRepository.save(it) }
        return savedGroup
    }

    override suspend fun updateGroup(
        group: Group?,
        name: String?,
        description: String?,
        leader: String?,
        leaderCreateChallenge: Boolean?
    ): Group? {
        if (group == null) {
            return null
        }
        val copiedGroup = localRepository.getUnmanagedCopy(group)
        copiedGroup.name = name
        copiedGroup.description = description
        copiedGroup.leaderID = leader
        copiedGroup.leaderOnlyChallenges = leaderCreateChallenge ?: false
        localRepository.save(copiedGroup)
        return apiClient.updateGroup(copiedGroup.id, copiedGroup)
    }

    override fun retrieveGroups(type: String): Flowable<List<Group>> {
        return apiClient.listGroups(type)
            .doOnNext { groups ->
                if ("guilds" == type) {
                    val memberships = groups.map {
                        GroupMembership(userID, it.id)
                    }
                    localRepository.saveGroupMemberships(userID, memberships)
                }
                localRepository.save(groups)
            }
    }

    override fun getGroups(type: String) = localRepository.getGroups(type)

    override fun getPublicGuilds() = localRepository.getPublicGuilds()

    override fun getInboxConversations() = localRepository.getInboxConversation(userID)

    override fun getInboxMessages(replyToUserID: String?) = localRepository.getInboxMessages(userID, replyToUserID)

    override suspend fun retrieveInboxMessages(uuid: String, page: Int): List<ChatMessage>? {
        val messages = apiClient.retrieveInboxMessages(uuid, page) ?: return null
        messages.forEach {
            it.isInboxMessage = true
        }
        localRepository.saveInboxMessages(userID, uuid, messages, page)
        return messages
    }

    override fun retrieveInboxConversations(): Flowable<List<InboxConversation>> {
        return apiClient.retrieveInboxConversations().doOnNext { conversations ->
            localRepository.saveInboxConversations(userID, conversations)
        }
    }

    override suspend fun postPrivateMessage(recipientId: String, messageObject: HashMap<String, String>): List<ChatMessage>? {
        val message = apiClient.postPrivateMessage(messageObject)
        return retrieveInboxMessages(recipientId, 0)
    }

    override suspend fun postPrivateMessage(recipientId: String, message: String): List<ChatMessage>? {
        val messageObject = HashMap<String, String>()
        messageObject["message"] = message
        messageObject["toUserId"] = recipientId
        return postPrivateMessage(recipientId, messageObject)
    }

    override suspend fun getGroupMembers(id: String) = localRepository.getGroupMembers(id)

    override suspend fun retrieveGroupMembers(id: String, includeAllPublicFields: Boolean): List<Member>? {
        val members = apiClient.getGroupMembers(id, includeAllPublicFields)
        members?.let { localRepository.saveGroupMembers(id, it) }
        return members
    }

    override fun inviteToGroup(id: String, inviteData: Map<String, Any>): Flowable<List<Void>> = apiClient.inviteToGroup(id, inviteData)

    override suspend fun retrieveMember(userId: String?): Member? {
        return if (userId == null) {
            null
        } else {
            try {
                apiClient.getMember(UUID.fromString(userId).toString())
            } catch (_: IllegalArgumentException) {
                apiClient.getMemberWithUsername(userId)
            }
        }
    }

    override suspend fun retrieveMemberWithUsername(username: String?): Member? {
        return retrieveMember(username)
    }

    override fun findUsernames(username: String, context: String?, id: String?): Flowable<List<FindUsernameResult>> {
        return apiClient.findUsernames(username, context, id)
    }

    override fun markPrivateMessagesRead(user: User?): Flowable<Void> {
        if (user?.isManaged == true) {
            localRepository.modify(user) {
                it.inbox?.hasUserSeenInbox = true
            }
        }
        return apiClient.markPrivateMessagesRead()
    }

    override fun markSomePrivateMessagesAsRead(user: User?, messages: List<ChatMessage>) {
        if (user?.isManaged == true) {
            val numOfUnseenMessages = messages.count { !it.isSeen }
            localRepository.modify(user) {
                val numOfNewMessagesFromInbox = it.inbox?.newMessages ?: 0
                if (numOfNewMessagesFromInbox > numOfUnseenMessages) {
                    it.inbox?.newMessages = numOfNewMessagesFromInbox - numOfUnseenMessages
                } else {
                    it.inbox?.newMessages = 0
                }
            }
        }
        for (message in messages.filter { it.isManaged && !it.isSeen }) {
            localRepository.modify(message) {
                it.isSeen = true
            }
        }
    }

    override fun getUserGroups(type: String?) = localRepository.getUserGroups(userID, type)

    override fun acceptQuest(user: User?, partyId: String): Flowable<Void> {
        return apiClient.acceptQuest(partyId)
            .doOnNext {
                user?.let {
                    localRepository.updateRSVPNeeded(it, false)
                }
            }
    }

    override fun rejectQuest(user: User?, partyId: String): Flowable<Void> {
        return apiClient.rejectQuest(partyId)
            .doOnNext { _ ->
                user?.let {
                    localRepository.updateRSVPNeeded(it, false)
                }
            }
    }

    override fun leaveQuest(partyId: String): Flowable<Void> {
        return apiClient.leaveQuest(partyId)
    }

    override fun cancelQuest(partyId: String): Flowable<Void> {
        return apiClient.cancelQuest(partyId)
            .doOnNext { localRepository.removeQuest(partyId) }
    }

    override fun abortQuest(partyId: String): Flowable<Quest> {
        return apiClient.abortQuest(partyId)
            .doOnNext { localRepository.removeQuest(partyId) }
    }

    override fun rejectGroupInvite(groupId: String): Flowable<Void> {
        return apiClient.rejectGroupInvite(groupId)
            .doOnNext {
                localRepository.rejectGroupInvitation(userID, groupId)
            }
    }

    override fun forceStartQuest(party: Group): Flowable<Quest> {
        return apiClient.forceStartQuest(party.id, localRepository.getUnmanagedCopy(party))
            .doOnNext { localRepository.setQuestActivity(party, true) }
    }

    override fun getMemberAchievements(userId: String?): Flowable<List<Achievement>> {
        return if (userId == null) {
            Flowable.empty()
        } else apiClient.getMemberAchievements(userId)
    }

    override fun transferGems(giftedID: String, amount: Int): Flowable<Void> {
        return apiClient.transferGems(giftedID, amount)
    }
}
