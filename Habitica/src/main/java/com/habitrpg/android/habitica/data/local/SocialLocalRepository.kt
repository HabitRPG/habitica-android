package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.social.GroupMembership
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.Flowable
import io.realm.RealmResults

interface SocialLocalRepository : BaseLocalRepository {
    fun getPublicGuilds(): Flowable<RealmResults<Group>>

    fun getUserGroups(userID: String): Flowable<RealmResults<Group>>
    fun getGroups(type: String): Flowable<RealmResults<Group>>

    fun getGroup(id: String): Flowable<Group>

    fun getGroupChat(groupId: String): Flowable<RealmResults<ChatMessage>>

    fun deleteMessage(id: String)

    fun getGroupMembers(partyId: String): Flowable<RealmResults<Member>>

    fun updateRSVPNeeded(user: User?, newValue: Boolean)

    fun likeMessage(chatMessage: ChatMessage, userId: String, liked: Boolean)

    fun saveGroupMembers(groupId: String?, members: List<Member>)

    fun removeQuest(partyId: String)

    fun setQuestActivity(party: Group?, active: Boolean)

    fun saveChatMessages(groupId: String?, chatMessages: List<ChatMessage>)

    fun doesGroupExist(id: String): Boolean
    fun updateMembership(userId: String, id: String, isMember: Boolean)
    fun getGroupMembership(userId: String, id: String): Flowable<GroupMembership>
    fun getGroupMemberships(userId: String): Flowable<RealmResults<GroupMembership>>
    fun rejectGroupInvitation(userID: String, groupID: String)

    fun getInboxMessages(userId: String, replyToUserID: String?): Flowable<RealmResults<ChatMessage>>

    fun getInboxOverviewList(userId: String): Flowable<RealmResults<ChatMessage>>
    fun saveGroupMemberships(userID: String?, memberships: List<GroupMembership>)
    fun saveInboxMessages(userID: String, messages: List<ChatMessage>)
    fun getChatMessage(messageID: String): Flowable<ChatMessage>
}
