package com.habitrpg.android.habitica.data.implementation

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
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.realm.RealmResults

class SocialRepositoryImpl(localRepository: SocialLocalRepository, apiClient: ApiClient, userID: String) : BaseRepositoryImpl<SocialLocalRepository>(localRepository, apiClient, userID), SocialRepository {
    override fun getChatmessage(messageID: String): Flowable<ChatMessage> {
        return localRepository.getChatMessage(messageID)
    }

    override fun getGroupMembership(id: String): Flowable<GroupMembership> {
        return localRepository.getGroupMembership(userID, id)
    }

    override fun getGroupMemberships(): Flowable<RealmResults<GroupMembership>> {
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
                .doOnSuccess { localRepository.saveChatMessages(groupId, it) }
    }

    override fun getGroupChat(groupId: String): Flowable<RealmResults<ChatMessage>> {
        return localRepository.getGroupChat(groupId)
    }

    override fun markMessagesSeen(seenGroupId: String) {
        apiClient.seenMessages(seenGroupId).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
    }

    override fun flagMessage(chatMessage: ChatMessage, additionalInfo: String): Flowable<Void> {
        return if (chatMessage.id == "") {
            Flowable.empty()
        } else {
            val data = mutableMapOf<String, String>()
            data["comment"] = additionalInfo
            apiClient.flagMessage(chatMessage.groupId ?: "", chatMessage.id, data)
        }
    }

    override fun likeMessage(chatMessage: ChatMessage): Flowable<ChatMessage> {
        if (chatMessage.id == "") {
            return Flowable.empty()
        }
        val liked = chatMessage.userLikesMessage(userID)
        localRepository.likeMessage(chatMessage, userID, !liked)
        return apiClient.likeMessage(chatMessage.groupId ?: "", chatMessage.id)
                .doOnError { localRepository.likeMessage(chatMessage, userID, liked) }
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
                .doOnNext { postChatMessageResult ->
                    if (postChatMessageResult != null) {
                        localRepository.save(postChatMessageResult.message)
                    }
                }
    }

    override fun postGroupChat(groupId: String, message: String): Flowable<PostChatMessageResult> {
        val messageObject = HashMap<String, String>()
        messageObject["message"] = message
        return postGroupChat(groupId, messageObject)
    }

    override fun retrieveGroup(id: String): Flowable<Group> {
        return Flowable.zip(apiClient.getGroup(id).doOnNext { localRepository.save(it) }, retrieveGroupChat(id)
                .map { message ->
                    message.forEach {
                        it.groupId = id
                    }
                    message
                }
                .doOnSuccess { localRepository.save(it) }.toFlowable(),
                BiFunction<Group, List<ChatMessage>, Group> { group, _ ->
                    group
                }
        )
    }

    override fun getGroup(id: String?): Flowable<Group> {
        if (id == null) {
            return Flowable.empty()
        }
        return localRepository.getGroup(id)
    }

    override fun leaveGroup(id: String?): Flowable<Group> {
        if (id == null) {
            return Flowable.empty()
        }
        return apiClient.leaveGroup(id)
                .doOnNext { localRepository.updateMembership(userID, id, false) }
                .flatMapMaybe { localRepository.getGroup(id).firstElement() }
    }

    override fun joinGroup(id: String?): Flowable<Group> {
        if (id == null) {
            return Flowable.empty()
        }
        return apiClient.joinGroup(id)
                .doOnNext { group ->
                    localRepository.updateMembership(userID, id, true)
                    localRepository.save(group)
                }
    }

    override fun createGroup(name: String?, description: String?, leader: String?, type: String?, privacy: String?, leaderCreateChallenge: Boolean?): Flowable<Group> {
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

    override fun updateGroup(group: Group?, name: String?, description: String?, leader: String?, leaderCreateChallenge: Boolean?): Flowable<Void> {
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

    override fun getGroups(type: String): Flowable<RealmResults<Group>> = localRepository.getGroups(type)

    override fun getPublicGuilds(): Flowable<RealmResults<Group>> = localRepository.getPublicGuilds()

    override fun getInboxOverviewList(): Flowable<RealmResults<ChatMessage>> = localRepository.getInboxOverviewList(userID)

    override fun getInboxMessages(replyToUserID: String?): Flowable<RealmResults<ChatMessage>> = localRepository.getInboxMessages(userID, replyToUserID)

    override fun retrieveInboxMessages(): Flowable<List<ChatMessage>> {
        return apiClient.retrieveInboxMessages().doOnNext { messages ->
            messages.forEach {
                it.isInboxMessage = true
            }
            localRepository.saveInboxMessages(userID, messages)
        }
    }

    override fun postPrivateMessage(messageObject: HashMap<String, String>): Flowable<List<ChatMessage>> {
        return apiClient.postPrivateMessage(messageObject).flatMap { retrieveInboxMessages() }
    }

    override fun postPrivateMessage(recipientId: String, message: String): Flowable<List<ChatMessage>> {
        val messageObject = HashMap<String, String>()
        messageObject["message"] = message
        messageObject["toUserId"] = recipientId
        return postPrivateMessage(messageObject)
    }

    override fun getGroupMembers(id: String): Flowable<RealmResults<Member>> = localRepository.getGroupMembers(id)

    override fun retrieveGroupMembers(id: String, includeAllPublicFields: Boolean): Flowable<List<Member>> {
        return apiClient.getGroupMembers(id, includeAllPublicFields)
                .doOnNext { members -> localRepository.saveGroupMembers(id, members) }
    }

    override fun inviteToGroup(id: String, inviteData: Map<String, Any>): Flowable<List<String>> = apiClient.inviteToGroup(id, inviteData)

    override fun getMember(userId: String?): Flowable<Member> {
        return if (userId == null) {
            Flowable.empty()
        } else apiClient.getMember(userId)
    }

    override fun getMemberWithUsername(username: String?): Flowable<Member> {
        return if (username == null) {
            Flowable.empty()
        } else apiClient.getMemberWithUsername(username)
    }

    override fun findUsernames(username: String, context: String?, id: String?): Flowable<List<FindUsernameResult>> {
        return apiClient.findUsernames(username, context, id)
    }

    override fun markPrivateMessagesRead(user: User?): Flowable<Void> {
        return apiClient.markPrivateMessagesRead()
                .doOnNext {
                    if (user?.isManaged == true) {
                        localRepository.executeTransaction { user.inbox?.newMessages = 0 }
                    }
                }
    }

    override fun getUserGroups(): Flowable<RealmResults<Group>> = localRepository.getUserGroups(userID)

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
}
