package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.local.SocialLocalRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.AchievementResult
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.responses.PostChatMessageResult
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.user.User
import io.realm.Realm
import io.realm.RealmResults
import rx.Observable
import rx.functions.Action1
import java.util.*


class SocialRepositoryImpl(localRepository: SocialLocalRepository, apiClient: ApiClient, private val userId: String) : BaseRepositoryImpl<SocialLocalRepository>(localRepository, apiClient), SocialRepository {

    override fun retrieveGroupChat(groupId: String): Observable<List<ChatMessage>> {
        return apiClient.listGroupChat(groupId)
                .flatMap { Observable.from(it) }
                .map { chatMessage ->
                    chatMessage.groupId = groupId
                    chatMessage
                }
                .toList()
                .doOnNext { chatMessages -> localRepository.saveChatMessages(groupId, chatMessages) }
    }

    override fun getGroupChat(groupId: String): Observable<RealmResults<ChatMessage>> {
        return localRepository.getGroupChat(groupId)
    }

    override fun markMessagesSeen(seenGroupId: String) {
        apiClient.seenMessages(seenGroupId).subscribe(Action1 { }, RxErrorHandler.handleEmptyError())
    }

    override fun flagMessage(chatMessage: ChatMessage): Observable<Void> {
        return if (chatMessage.id == "") {
            Observable.just(null)
        } else apiClient.flagMessage(chatMessage.groupId ?: "", chatMessage.id)
    }

    override fun likeMessage(chatMessage: ChatMessage): Observable<ChatMessage> {
        if (chatMessage.id == "") {
            return Observable.just(null)
        }
        val liked = chatMessage.userLikesMessage(userId)
        localRepository.likeMessage(chatMessage, userId, !liked)
        return apiClient.likeMessage(chatMessage.groupId ?: "", chatMessage.id)
                .doOnError { localRepository.likeMessage(chatMessage, userId, liked) }
    }

    override fun deleteMessage(chatMessage: ChatMessage): Observable<Void> {
        return apiClient.deleteMessage(chatMessage.groupId ?: "", chatMessage.id)
                .doOnNext { localRepository.deleteMessage(chatMessage.id) }
    }

    override fun postGroupChat(groupId: String, messageObject: HashMap<String, String>): Observable<PostChatMessageResult> {
        return apiClient.postGroupChat(groupId, messageObject)
                .map { postChatMessageResult ->
                    if (postChatMessageResult != null) {
                        postChatMessageResult.message.groupId = groupId
                    }
                    postChatMessageResult
                }
                .doOnNext { postChatMessageResult ->
                    if (postChatMessageResult != null) {
                        localRepository.save(postChatMessageResult.message)
                    }
                }
    }

    override fun postGroupChat(groupId: String, message: String): Observable<PostChatMessageResult> {
        val messageObject = HashMap<String, String>()
        messageObject["message"] = message
        return postGroupChat(groupId, messageObject)
    }

    override fun retrieveGroup(id: String): Observable<Group> {
        var observable = apiClient.getGroup(id)
        if ("party" != id && localRepository.doesGroupExist(id)) {
            observable = observable.withLatestFrom(localRepository.getGroup(id)
                    .first()) { newGroup, oldGroup ->
                newGroup.isMember = oldGroup.isMember
                newGroup
            }
        }
        return Observable.zip(observable.doOnNext { localRepository.save(it) }, retrieveGroupChat(id)
                .map {
                    it.forEach {
                        it.groupId = id
                    }
                    return@map it
                }
                .doOnNext { localRepository.save(it) }
        ) { group, _ ->
            return@zip group
        }

    }

    override fun getGroup(id: String?): Observable<Group> {
        if (id == null) {
            return Observable.just(null)
        }
        return localRepository.getGroup(id)
    }

    override fun leaveGroup(id: String?): Observable<Group> {
        if (id == null) {
            return Observable.just(null)
        }
        return apiClient.leaveGroup(id)
                .flatMap { localRepository.getGroup(id).first() }
                .doOnNext { group -> localRepository.executeTransaction { group.isMember = false } }
    }

    override fun joinGroup(id: String?): Observable<Group> {
        if (id == null) {
            return Observable.just(null)
        }
        return apiClient.joinGroup(id)
                .doOnNext { group ->
                    group?.isMember = true
                    localRepository.save(group)
                }
    }

    override fun updateGroup(group: Group?, name: String?, description: String?, leader: String?, privacy: String?): Observable<Void> {
        if (group == null) {
            return Observable.just(null)
        }
        val copiedGroup = localRepository.getUnmanagedCopy(group)
        copiedGroup.name = name
        copiedGroup.description = description
        copiedGroup.leaderID = leader
        copiedGroup.privacy = privacy
        localRepository.save(copiedGroup)
        return apiClient.updateGroup(copiedGroup.id, copiedGroup)
    }

    override fun retrieveGroups(type: String): Observable<List<Group>> {
        return apiClient.listGroups(type)
                .doOnNext { groups ->
                    if ("guilds" == type) {
                        groups.forEach { guild -> guild.isMember = true }
                    }
                    localRepository.save(groups)
                }
    }

    override fun getGroups(type: String): Observable<RealmResults<Group>> = localRepository.getGroups(type)

    override fun getPublicGuilds(): Observable<RealmResults<Group>> = localRepository.getPublicGuilds()

    override fun postPrivateMessage(messageObject: HashMap<String, String>): Observable<PostChatMessageResult> {
        return apiClient.postPrivateMessage(messageObject)
    }

    override fun postPrivateMessage(recipientId: String, message: String): Observable<PostChatMessageResult> {
        val messageObject = HashMap<String, String>()
        messageObject["message"] = message
        messageObject["toUserId"] = recipientId
        return postPrivateMessage(messageObject)
    }

    override fun getGroupMembers(id: String): Observable<RealmResults<Member>> = localRepository.getGroupMembers(id)

    override fun retrieveGroupMembers(id: String, includeAllPublicFields: Boolean): Observable<List<Member>> {
        return apiClient.getGroupMembers(id, includeAllPublicFields)
                .doOnNext { members -> localRepository.saveGroupMembers(id, members) }
    }

    override fun inviteToGroup(id: String, inviteData: Map<String, Any>): Observable<List<String>> = apiClient.inviteToGroup(id, inviteData)

    override fun getUserChallenges(): Observable<List<Challenge>> = apiClient.getUserChallenges()

    override fun getMember(userId: String?): Observable<Member> {
        return if (userId == null) {
            Observable.just(null)
        } else apiClient.getMember(userId)
    }

    override fun markPrivateMessagesRead(user: User?): Observable<Void> {
        return apiClient.markPrivateMessagesRead()
                .doOnNext {
                    if (user?.isManaged == true) {
                        localRepository.executeTransaction { user.inbox.newMessages = 0 }
                    }
                }
    }

    override fun getUserGroups(): Observable<RealmResults<Group>> = localRepository.getUserGroups()

    override fun acceptQuest(user: User, partyId: String): Observable<Void> {
        return apiClient.acceptQuest(partyId)
                .doOnNext { localRepository.updateRSVPNeeded(user, false) }
    }

    override fun rejectQuest(user: User, partyId: String): Observable<Void> {
        return apiClient.rejectQuest(partyId)
                .doOnNext { localRepository.updateRSVPNeeded(user, false) }
    }

    override fun leaveQuest(partyId: String?): Observable<Void> {
        return apiClient.leaveQuest(partyId)
    }

    override fun cancelQuest(partyId: String?): Observable<Void> {
        if (partyId == null) {
            return Observable.just(null)
        }
        return apiClient.cancelQuest(partyId)
                .doOnNext { localRepository.removeQuest(partyId) }
    }

    override fun abortQuest(partyId: String?): Observable<Quest> {
        if (partyId == null) {
            return Observable.just(null)
        }
        return apiClient.abortQuest(partyId)
                .doOnNext { localRepository.removeQuest(partyId) }
    }

    override fun rejectGroupInvite(groupId: String): Observable<Void> {
        return apiClient.rejectQuest(groupId)
    }

    override fun forceStartQuest(party: Group): Observable<Quest> {
        return apiClient.forceStartQuest(party.id, localRepository.getUnmanagedCopy(party))
                .doOnNext { localRepository.setQuestActivity(party, true) }
    }

    override fun getMemberAchievements(userId: String?): Observable<AchievementResult> {
        return if (userId == null) {
            Observable.just(null)
        } else apiClient.getMemberAchievements(userId)
    }
}
