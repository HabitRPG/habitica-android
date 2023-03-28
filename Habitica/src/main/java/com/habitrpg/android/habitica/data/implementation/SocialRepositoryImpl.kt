package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.local.SocialLocalRepository
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
        return retrievePartyMembers(groupID, true)
    }

    override suspend fun blockMember(userID: String): List<String>? {
        return apiClient.blockMember(userID)
    }

    override fun getMember(userID: String?): Flow<Member?> {
        return localRepository.getMember(userID)
    }

    override suspend fun updateMember(memberID: String, key: String, value: Any?): Member? {
        return apiClient.updateMember(memberID, mapOf(key to value))
    }

    override suspend fun retrievePartySeekingUsers(page: Int) : List<Member>? {
        return apiClient.retrievePartySeekingUsers(page)
    }

    override fun getGroupMembership(id: String) = localRepository.getGroupMembership(userID, id)

    override fun getGroupMemberships(): Flow<List<GroupMembership>> {
        return localRepository.getGroupMemberships(userID)
    }

    override suspend fun retrieveGroupChat(groupId: String): List<ChatMessage>? {
        val messages = apiClient.listGroupChat(groupId)
        messages?.forEach { it.groupId = groupId }
        return messages
    }

    override fun getGroupChat(groupId: String): Flow<List<ChatMessage>> {
        return localRepository.getGroupChat(groupId)
    }

    override suspend fun markMessagesSeen(seenGroupId: String) {
        apiClient.seenMessages(seenGroupId)
    }

    override suspend fun flagMessage(chatMessageID: String, additionalInfo: String, groupID: String?): Void? {
        return when {
            chatMessageID.isBlank() -> return null
            userID == BuildConfig.ANDROID_TESTING_UUID -> return null
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

    override suspend fun likeMessage(chatMessage: ChatMessage): ChatMessage? {
        if (chatMessage.id.isBlank()) {
            return null
        }
        val liked = chatMessage.userLikesMessage(userID)
        localRepository.likeMessage(chatMessage, userID, !liked)
        val message = apiClient.likeMessage(chatMessage.groupId ?: "", chatMessage.id)
        message?.groupId = chatMessage.groupId
        message?.let { localRepository.save(it) }
        return null
    }

    override suspend fun deleteMessage(chatMessage: ChatMessage): Void? {
        if (chatMessage.isInboxMessage) {
            apiClient.deleteInboxMessage(chatMessage.id)
        } else {
            apiClient.deleteMessage(chatMessage.groupId ?: "", chatMessage.id)
        }
        localRepository.deleteMessage(chatMessage.id)
        return null
    }

    override suspend fun postGroupChat(groupId: String, messageObject: HashMap<String, String>): PostChatMessageResult? {
        val result = apiClient.postGroupChat(groupId, messageObject)
        result?.message?.groupId = groupId
        return result
    }

    override suspend fun postGroupChat(groupId: String, message: String): PostChatMessageResult? {
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

    override suspend fun retrieveGroups(type: String): List<Group>? {
        val groups = apiClient.listGroups(type) ?: return null
        if ("guilds" == type) {
            val memberships = groups.map {
                GroupMembership(userID, it.id)
            }
            localRepository.saveGroupMemberships(userID, memberships)
        }
        localRepository.save(groups)
        return groups
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

    override suspend fun retrieveInboxConversations(): List<InboxConversation>? {
        val conversations = apiClient.retrieveInboxConversations() ?: return null
        localRepository.saveInboxConversations(userID, conversations)
        return conversations
    }

    override suspend fun postPrivateMessage(recipientId: String, messageObject: HashMap<String, String>): List<ChatMessage>? {
        apiClient.postPrivateMessage(messageObject)
        return retrieveInboxMessages(recipientId, 0)
    }

    override suspend fun postPrivateMessage(recipientId: String, message: String): List<ChatMessage>? {
        val messageObject = HashMap<String, String>()
        messageObject["message"] = message
        messageObject["toUserId"] = recipientId
        return postPrivateMessage(recipientId, messageObject)
    }

    override suspend fun getPartyMembers(id: String) = localRepository.getPartyMembers(id)
    override suspend fun getGroupMembers(id: String) = localRepository.getGroupMembers(id)

    override suspend fun retrievePartyMembers(id: String, includeAllPublicFields: Boolean): List<Member>? {
        val members = apiClient.getGroupMembers(id, includeAllPublicFields)
        members?.let { localRepository.savePartyMembers(id, it) }
        return members
    }

    override suspend fun inviteToGroup(id: String, inviteData: Map<String, Any>) = apiClient.inviteToGroup(id, inviteData)

    override suspend fun retrieveMember(userId: String?, fromHall: Boolean): Member? {
        return if (userId == null) {
            null
        } else {
            try {
                if (fromHall) {
                    apiClient.getHallMember(userId)
                } else {
                    apiClient.getMember(UUID.fromString(userId).toString())
                }
            } catch (_: IllegalArgumentException) {
                apiClient.getMemberWithUsername(userId)
            }
        }
    }

    override suspend fun retrieveMemberWithUsername(username: String?, fromHall: Boolean): Member? {
        return retrieveMember(username, fromHall)
    }

    override suspend fun findUsernames(username: String, context: String?, id: String?): List<FindUsernameResult>? {
        return apiClient.findUsernames(username, context, id)
    }

    override suspend fun markPrivateMessagesRead(user: User?) {
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

    override suspend fun acceptQuest(user: User?, partyId: String): Void? {
        apiClient.acceptQuest(partyId)
        user?.let {
            localRepository.updateRSVPNeeded(it, false)
        }
        return null
    }

    override suspend fun rejectQuest(user: User?, partyId: String): Void? {
        apiClient.rejectQuest(partyId)
        user?.let {
            localRepository.updateRSVPNeeded(it, false)
        }
        return null
    }

    override suspend fun leaveQuest(partyId: String): Void? {
        return apiClient.leaveQuest(partyId)
    }

    override suspend fun cancelQuest(partyId: String): Void? {
        apiClient.cancelQuest(partyId)
        localRepository.removeQuest(partyId)
        return null
    }

    override suspend fun abortQuest(partyId: String): Quest? {
        val quest = apiClient.abortQuest(partyId)
        localRepository.removeQuest(partyId)
        return quest
    }

    override suspend fun rejectGroupInvite(groupId: String): Void? {
        apiClient.rejectGroupInvite(groupId)
        localRepository.rejectGroupInvitation(userID, groupId)
        return null
    }

    override suspend fun forceStartQuest(party: Group): Quest? {
        val quest = apiClient.forceStartQuest(party.id, localRepository.getUnmanagedCopy(party))
        localRepository.setQuestActivity(party, true)
        return quest
    }

    override suspend fun getMemberAchievements(userId: String?): List<Achievement>? {
        return if (userId == null) {
            null
        } else apiClient.getMemberAchievements(userId)
    }

    override suspend fun transferGems(giftedID: String, amount: Int): Void? {
        return apiClient.transferGems(giftedID, amount)
    }
}
