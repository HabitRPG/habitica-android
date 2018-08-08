package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.local.SocialLocalRepository
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.AchievementResult
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.responses.PostChatMessageResult
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.social.GroupMembership
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.realm.RealmResults

class SocialRepositoryImpl(localRepository: SocialLocalRepository, apiClient: ApiClient, private val userId: String) : BaseRepositoryImpl<SocialLocalRepository>(localRepository, apiClient), SocialRepository {
    override fun getGroupMembership(id: String): Flowable<GroupMembership> {
        return localRepository.getGroupMembership(userId, id)
    }

    override fun getGroupMemberships(): Flowable<RealmResults<GroupMembership>> {
        return localRepository.getGroupMemberships(userId)
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

    override fun flagMessage(chatMessage: ChatMessage): Flowable<Void> {
        return if (chatMessage.id == "") {
            Flowable.empty()
        } else apiClient.flagMessage(chatMessage.groupId ?: "", chatMessage.id)
    }

    override fun likeMessage(chatMessage: ChatMessage): Flowable<ChatMessage> {
        if (chatMessage.id == "") {
            return Flowable.empty()
        }
        val liked = chatMessage.userLikesMessage(userId)
        localRepository.likeMessage(chatMessage, userId, !liked)
        return apiClient.likeMessage(chatMessage.groupId ?: "", chatMessage.id)
                .doOnError { localRepository.likeMessage(chatMessage, userId, liked) }
    }

    override fun deleteMessage(chatMessage: ChatMessage): Flowable<Void> {
        return apiClient.deleteMessage(chatMessage.groupId ?: "", chatMessage.id)
                .doOnNext { localRepository.deleteMessage(chatMessage.id) }
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
                .map {
                    it.forEach {
                        it.groupId = id
                    }
                    it
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
                .flatMapMaybe { localRepository.getGroup(id).firstElement() }
                .doOnNext { localRepository.executeTransaction { localRepository.updateMembership(userId, id, false) } }
    }

    override fun joinGroup(id: String?): Flowable<Group> {
        if (id == null) {
            return Flowable.empty()
        }
        return apiClient.joinGroup(id)
                .doOnNext { group ->
                    localRepository.updateMembership(userId, id, true)
                    localRepository.save(group)
                }
    }

    override fun updateGroup(group: Group?, name: String?, description: String?, leader: String?, privacy: String?): Flowable<Void> {
        if (group == null) {
            return Flowable.empty()
        }
        val copiedGroup = localRepository.getUnmanagedCopy(group)
        copiedGroup.name = name
        copiedGroup.description = description
        copiedGroup.leaderID = leader
        copiedGroup.privacy = privacy
        localRepository.save(copiedGroup)
        return apiClient.updateGroup(copiedGroup.id, copiedGroup)
    }

    override fun retrieveGroups(type: String): Flowable<List<Group>> {
        return apiClient.listGroups(type)
                .doOnNext { groups ->
                    if ("guilds" == type) {
                        val memberships = groups.map {
                            GroupMembership(userId, it.id)
                        }
                        localRepository.save(memberships)
                    }
                    localRepository.save(groups)
                }
    }

    override fun getGroups(type: String): Flowable<RealmResults<Group>> = localRepository.getGroups(type)

    override fun getPublicGuilds(): Flowable<RealmResults<Group>> = localRepository.getPublicGuilds()

    override fun postPrivateMessage(messageObject: HashMap<String, String>): Flowable<PostChatMessageResult> {
        return apiClient.postPrivateMessage(messageObject)
    }

    override fun postPrivateMessage(recipientId: String, message: String): Flowable<PostChatMessageResult> {
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

    override fun getUserChallenges(): Flowable<List<Challenge>> = apiClient.userChallenges

    override fun getMember(userId: String?): Flowable<Member> {
        return if (userId == null) {
            Flowable.empty()
        } else apiClient.getMember(userId)
    }

    override fun markPrivateMessagesRead(user: User?): Flowable<Void> {
        return apiClient.markPrivateMessagesRead()
                .doOnNext {
                    if (user?.isManaged == true) {
                        localRepository.executeTransaction { user.inbox?.newMessages = 0 }
                    }
                }
    }

    override fun getUserGroups(): Flowable<RealmResults<Group>> = localRepository.getUserGroups(userId)

    override fun acceptQuest(user: User?, partyId: String): Flowable<Void> {
        return apiClient.acceptQuest(partyId)
                .doOnNext {
                    user.notNull {
                        localRepository.updateRSVPNeeded(it, false)
                    }
                }
    }

    override fun rejectQuest(user: User?, partyId: String): Flowable<Void> {
        return apiClient.rejectQuest(partyId)
                .doOnNext {
                    user.notNull {
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
                    localRepository.rejectGroupInvitation(userId, groupId)
                }
    }

    override fun forceStartQuest(party: Group): Flowable<Quest> {
        return apiClient.forceStartQuest(party.id, localRepository.getUnmanagedCopy(party))
                .doOnNext { localRepository.setQuestActivity(party, true) }
    }

    override fun getMemberAchievements(userId: String?): Flowable<AchievementResult> {
        return if (userId == null) {
            Flowable.empty()
        } else apiClient.getMemberAchievements(userId)
    }
}
