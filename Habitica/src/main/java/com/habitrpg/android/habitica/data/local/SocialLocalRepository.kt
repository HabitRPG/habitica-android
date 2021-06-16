package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.social.GroupMembership
import com.habitrpg.android.habitica.models.social.InboxConversation
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.rxjava3.core.Flowable

interface SocialLocalRepository : BaseLocalRepository {
    fun getPublicGuilds(): Flowable<out List<Group>>

    fun getUserGroups(userID: String, type: String?): Flowable<out List<Group>>
    fun getGroups(type: String): Flowable<out List<Group>>

    fun getGroup(id: String): Flowable<Group>
    fun saveGroup(group: Group)

    fun getGroupChat(groupId: String): Flowable<out List<ChatMessage>>

    fun deleteMessage(id: String)

    fun getGroupMembers(partyId: String): Flowable<out List<Member>>

    fun updateRSVPNeeded(user: User?, newValue: Boolean)

    fun likeMessage(chatMessage: ChatMessage, userId: String, liked: Boolean)

    fun saveGroupMembers(groupId: String?, members: List<Member>)

    fun removeQuest(partyId: String)

    fun setQuestActivity(party: Group?, active: Boolean)

    fun saveChatMessages(groupId: String?, chatMessages: List<ChatMessage>)

    fun doesGroupExist(id: String): Boolean
    fun updateMembership(userId: String, id: String, isMember: Boolean)
    fun getGroupMembership(userId: String, id: String): Flowable<GroupMembership>
    fun getGroupMemberships(userId: String): Flowable<out List<GroupMembership>>
    fun rejectGroupInvitation(userID: String, groupID: String)

    fun getInboxMessages(userId: String, replyToUserID: String?): Flowable<out List<ChatMessage>>

    fun getInboxConversation(userId: String): Flowable<out List<InboxConversation>>
    fun saveGroupMemberships(userID: String?, memberships: List<GroupMembership>)
    fun saveInboxMessages(userID: String, recipientID: String, messages: List<ChatMessage>, page: Int)
    fun saveInboxConversations(userID: String, conversations: List<InboxConversation>)
    fun getChatMessage(messageID: String): Flowable<ChatMessage>
}
