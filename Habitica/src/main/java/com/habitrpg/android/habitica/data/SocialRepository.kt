package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.AchievementResult
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.responses.PostChatMessageResult
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.user.User

import java.util.HashMap

import io.realm.RealmResults
import rx.Observable

interface SocialRepository : BaseRepository {
    fun getPublicGuilds(): Observable<RealmResults<Group>>

    fun getUserChallenges(): Observable<List<Challenge>>

    fun getUserGroups(): Observable<RealmResults<Group>>
    fun retrieveGroupChat(groupId: String): Observable<List<ChatMessage>>
    fun getGroupChat(groupId: String): Observable<RealmResults<ChatMessage>>

    fun markMessagesSeen(seenGroupId: String)

    fun flagMessage(chatMessage: ChatMessage): Observable<Void>

    fun likeMessage(chatMessage: ChatMessage): Observable<ChatMessage>

    fun deleteMessage(chatMessage: ChatMessage): Observable<Void>

    fun postGroupChat(groupId: String, messageObject: HashMap<String, String>): Observable<PostChatMessageResult>
    fun postGroupChat(groupId: String, message: String): Observable<PostChatMessageResult>

    fun retrieveGroup(id: String): Observable<Group>
    fun getGroup(id: String?): Observable<Group>

    fun leaveGroup(id: String?): Observable<Group>

    fun joinGroup(id: String?): Observable<Group>

    fun updateGroup(group: Group?, name: String?, description: String?, leader: String?, privacy: String?): Observable<Void>

    fun retrieveGroups(type: String): Observable<List<Group>>
    fun getGroups(type: String): Observable<RealmResults<Group>>

    fun postPrivateMessage(messageObject: HashMap<String, String>): Observable<PostChatMessageResult>
    fun postPrivateMessage(recipientId: String, message: String): Observable<PostChatMessageResult>


    fun getGroupMembers(id: String): Observable<RealmResults<Member>>
    fun retrieveGroupMembers(id: String, includeAllPublicFields: Boolean): Observable<List<Member>>

    fun inviteToGroup(id: String, inviteData: Map<String, Any>): Observable<List<String>>

    fun getMember(userId: String?): Observable<Member>

    fun markPrivateMessagesRead(user: User?): Observable<Void>

    fun acceptQuest(user: User, partyId: String): Observable<Void>
    fun rejectQuest(user: User, partyId: String): Observable<Void>

    fun leaveQuest(partyId: String?): Observable<Void>

    fun cancelQuest(partyId: String?): Observable<Void>

    fun abortQuest(partyId: String?): Observable<Quest>

    fun rejectGroupInvite(groupId: String): Observable<Void>

    fun forceStartQuest(party: Group): Observable<Quest>

    fun getMemberAchievements(userId: String?): Observable<AchievementResult>
}
