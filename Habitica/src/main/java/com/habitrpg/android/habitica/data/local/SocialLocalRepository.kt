package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.user.User

import io.realm.RealmResults
import rx.Observable

interface SocialLocalRepository : BaseLocalRepository {
    fun getPublicGuilds(): Observable<RealmResults<Group>>

    fun getUserGroups(): Observable<RealmResults<Group>>
    fun getGroups(type: String): Observable<RealmResults<Group>>

    fun getGroup(id: String): Observable<Group>

    fun getGroupChat(groupId: String): Observable<RealmResults<ChatMessage>>

    fun deleteMessage(id: String)

    fun getGroupMembers(partyId: String): Observable<RealmResults<Member>>

    fun updateRSVPNeeded(user: User?, newValue: Boolean)

    fun likeMessage(chatMessage: ChatMessage, userId: String, liked: Boolean)

    fun saveGroupMembers(groupId: String?, members: List<Member>)

    fun removeQuest(partyId: String)

    fun setQuestActivity(party: Group?, active: Boolean)

    fun saveChatMessages(groupId: String?, chatMessages: List<ChatMessage>)

    fun doesGroupExist(id: String): Boolean
}
