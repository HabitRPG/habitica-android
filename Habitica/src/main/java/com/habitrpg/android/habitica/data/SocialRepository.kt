package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.AchievementResult
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.responses.PostChatMessageResult
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.Flowable
import io.reactivex.Single
import io.realm.RealmResults
import java.util.*

interface SocialRepository : BaseRepository {
    fun getPublicGuilds(): Flowable<RealmResults<Group>>

    fun getUserChallenges(): Flowable<List<Challenge>>

    fun getUserGroups(): Flowable<RealmResults<Group>>
    fun retrieveGroupChat(groupId: String): Single<List<ChatMessage>>
    fun getGroupChat(groupId: String): Flowable<RealmResults<ChatMessage>>

    fun markMessagesSeen(seenGroupId: String)

    fun flagMessage(chatMessage: ChatMessage): Flowable<Void>

    fun likeMessage(chatMessage: ChatMessage): Flowable<ChatMessage>

    fun deleteMessage(chatMessage: ChatMessage): Flowable<Void>

    fun postGroupChat(groupId: String, messageObject: HashMap<String, String>): Flowable<PostChatMessageResult>
    fun postGroupChat(groupId: String, message: String): Flowable<PostChatMessageResult>

    fun retrieveGroup(id: String): Flowable<Group>
    fun getGroup(id: String?): Flowable<Group>

    fun leaveGroup(id: String?): Flowable<Group>

    fun joinGroup(id: String?): Flowable<Group>

    fun updateGroup(group: Group?, name: String?, description: String?, leader: String?, privacy: String?): Flowable<Void>

    fun retrieveGroups(type: String): Flowable<List<Group>>
    fun getGroups(type: String): Flowable<RealmResults<Group>>

    fun postPrivateMessage(messageObject: HashMap<String, String>): Flowable<PostChatMessageResult>
    fun postPrivateMessage(recipientId: String, message: String): Flowable<PostChatMessageResult>


    fun getGroupMembers(id: String): Flowable<RealmResults<Member>>
    fun retrieveGroupMembers(id: String, includeAllPublicFields: Boolean): Flowable<List<Member>>

    fun inviteToGroup(id: String, inviteData: Map<String, Any>): Flowable<List<String>>

    fun getMember(userId: String?): Flowable<Member>

    fun markPrivateMessagesRead(user: User?): Flowable<Void>

    fun acceptQuest(user: User, partyId: String): Flowable<Void>
    fun rejectQuest(user: User, partyId: String): Flowable<Void>

    fun leaveQuest(partyId: String?): Flowable<Void>

    fun cancelQuest(partyId: String?): Flowable<Void>

    fun abortQuest(partyId: String?): Flowable<Quest>

    fun rejectGroupInvite(groupId: String): Flowable<Void>

    fun forceStartQuest(party: Group): Flowable<Quest>

    fun getMemberAchievements(userId: String?): Flowable<AchievementResult>
}
