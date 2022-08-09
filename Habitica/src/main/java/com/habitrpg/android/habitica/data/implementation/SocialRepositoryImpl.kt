package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.local.SocialLocalRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
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
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.UUID

class SocialRepositoryImpl(
    localRepository: SocialLocalRepository,
    apiClient: ApiClient,
    userID: String
) : BaseRepositoryImpl<SocialLocalRepository>(localRepository, apiClient, userID), SocialRepository {
    override fun transferGroupOwnership(groupID: String, userID: String): Flowable<Group> {
        return localRepository.getGroupFlowable(groupID)
            .map {
                val group = localRepository.getUnmanagedCopy(it)
                group.leaderID = userID
                group
            }
            .flatMap {
                apiClient.updateGroup(it.id, it)
            }
    }

    override fun removeMemberFromGroup(groupID: String, userID: String): Flowable<List<Member>> {
        return apiClient.removeMemberFromGroup(groupID, userID)
            .flatMap {
                retrieveGroupMembers(groupID, true)
            }
    }

    override fun blockMember(userID: String): Flowable<List<String>> {
        return apiClient.blockMember(userID)
    }

    override fun getGroupMembership(id: String): Flowable<GroupMembership> {
        return localRepository.getGroupMembership(userID, id)
    }

    override fun getGroupMemberships(): Flowable<out List<GroupMembership>> {
        return localRepository.getGroupMemberships(userID)
    }

    override fun retrieveGroupChat(groupId: String): Single<List<ChatMessage>> {
        return apiClient.listGroupChat(groupId)
            .flatMap { Flowable.fromIterable(it) }
            .map { chatMessage ->
                chatMessage.groupId = groupId
                chatMessage
            }
            .toList()
    }

    override fun getGroupChat(groupId: String): Flowable<out List<ChatMessage>> {
        return localRepository.getGroupChat(groupId)
    }

    override fun markMessagesSeen(seenGroupId: String) {
        apiClient.seenMessages(seenGroupId).subscribe({ }, RxErrorHandler.handleEmptyError())
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

    override fun retrieveGroup(id: String): Flowable<Group> {
        return Flowable.zip(
            apiClient.getGroup(id).doOnNext { localRepository.saveGroup(it) },
            retrieveGroupChat(id)
                .toFlowable()
        ) { group, _ ->
            group
        }.doOnError {
            if (it is HttpException && it.code() == 404) {
                MainScope().launch {
                    val group = localRepository.getGroup(id).first()
                    if (group != null) {
                        localRepository.delete(group)
                    }
                }
            }
        }
    }

    override fun getGroup(id: String?) = id?.let { localRepository.getGroup(it) } ?: emptyFlow()
    override fun getGroupFlowable(id: String?): Flowable<Group> = id?.let { localRepository.getGroupFlowable(it) } ?: Flowable.empty()

    override fun leaveGroup(id: String?, keepChallenges: Boolean): Flowable<Group> {
        if (id?.isNotBlank() != true) {
            return Flowable.empty()
        }
        return apiClient.leaveGroup(id, if (keepChallenges) "remain-in-challenges" else "leave-challenges")
            .doOnNext { localRepository.updateMembership(userID, id, false) }
            .flatMapMaybe { localRepository.getGroupFlowable(id).firstElement() }
    }

    override fun joinGroup(id: String?): Flowable<Group> {
        if (id?.isNotBlank() != true) {
            return Flowable.empty()
        }
        return apiClient.joinGroup(id)
            .doOnNext { group ->
                localRepository.updateMembership(userID, id, true)
                localRepository.save(group)
            }
    }

    override fun createGroup(
        name: String?,
        description: String?,
        leader: String?,
        type: String?,
        privacy: String?,
        leaderCreateChallenge: Boolean?
    ): Flowable<Group> {
        val group = Group()
        group.name = name
        group.description = description
        group.type = type
        group.leaderID = leader
        group.privacy = privacy
        return apiClient.createGroup(group).doOnNext {
            localRepository.save(it)
        }
    }

    override fun updateGroup(
        group: Group?,
        name: String?,
        description: String?,
        leader: String?,
        leaderCreateChallenge: Boolean?
    ): Flowable<Group> {
        if (group == null) {
            return Flowable.empty()
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

    override fun getGroups(type: String): Flowable<out List<Group>> = localRepository.getGroups(type)

    override fun getPublicGuilds(): Flowable<out List<Group>> = localRepository.getPublicGuilds()

    override fun getInboxConversations(): Flowable<out List<InboxConversation>> = localRepository.getInboxConversation(userID)

    override fun getInboxMessages(replyToUserID: String?): Flowable<out List<ChatMessage>> = localRepository.getInboxMessages(userID, replyToUserID)

    override fun retrieveInboxMessages(uuid: String, page: Int): Flowable<List<ChatMessage>> {
        return apiClient.retrieveInboxMessages(uuid, page).doOnNext { messages ->
            messages.forEach {
                it.isInboxMessage = true
            }
            localRepository.saveInboxMessages(userID, uuid, messages, page)
        }
    }

    override fun retrieveInboxConversations(): Flowable<List<InboxConversation>> {
        return apiClient.retrieveInboxConversations().doOnNext { conversations ->
            localRepository.saveInboxConversations(userID, conversations)
        }
    }

    override fun postPrivateMessage(recipientId: String, messageObject: HashMap<String, String>): Flowable<List<ChatMessage>> {
        return apiClient.postPrivateMessage(messageObject).flatMap { retrieveInboxMessages(recipientId, 0) }
    }

    override fun postPrivateMessage(recipientId: String, message: String): Flowable<List<ChatMessage>> {
        val messageObject = HashMap<String, String>()
        messageObject["message"] = message
        messageObject["toUserId"] = recipientId
        return postPrivateMessage(recipientId, messageObject)
    }

    override fun getGroupMembers(id: String) = localRepository.getGroupMembers(id)

    override fun retrieveGroupMembers(id: String, includeAllPublicFields: Boolean): Flowable<List<Member>> {
        return apiClient.getGroupMembers(id, includeAllPublicFields)
            .doOnNext { members -> localRepository.saveGroupMembers(id, members) }
    }

    override fun inviteToGroup(id: String, inviteData: Map<String, Any>): Flowable<List<Void>> = apiClient.inviteToGroup(id, inviteData)

    override fun getMember(userId: String?): Flowable<Member> {
        return if (userId == null) {
            Flowable.empty()
        } else {
            try {
                apiClient.getMember(UUID.fromString(userId).toString())
            } catch (_: IllegalArgumentException) {
                apiClient.getMemberWithUsername(userId)
            }
        }
    }

    override fun getMemberWithUsername(username: String?): Flowable<Member> {
        return getMember(username)
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

    override fun getUserGroups(type: String?): Flowable<out List<Group>> = localRepository.getUserGroups(userID, type)

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
