package com.habitrpg.android.habitica.data

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
import kotlinx.coroutines.flow.Flow

interface SocialRepository : BaseRepository {
    fun getPublicGuilds(): Flowable<out List<Group>>

    fun getUserGroups(type: String?): Flowable<out List<Group>>
    fun retrieveGroupChat(groupId: String): Single<List<ChatMessage>>
    fun getGroupChat(groupId: String): Flowable<out List<ChatMessage>>

    fun markMessagesSeen(seenGroupId: String)

    fun flagMessage(
        chatMessageID: String,
        additionalInfo: String,
        groupID: String? = null
    ): Flowable<Void>

    fun likeMessage(chatMessage: ChatMessage): Flowable<ChatMessage>

    fun deleteMessage(chatMessage: ChatMessage): Flowable<Void>

    fun postGroupChat(
        groupId: String,
        messageObject: HashMap<String, String>
    ): Flowable<PostChatMessageResult>

    fun postGroupChat(groupId: String, message: String): Flowable<PostChatMessageResult>

    fun retrieveGroup(id: String): Flowable<Group>
    fun getGroup(id: String?): Flow<Group?>
    fun getGroupFlowable(id: String?): Flowable<Group>

    fun leaveGroup(id: String?, keepChallenges: Boolean): Flowable<Group>

    fun joinGroup(id: String?): Flowable<Group>

    fun createGroup(
        name: String?,
        description: String?,
        leader: String?,
        type: String?,
        privacy: String?,
        leaderCreateChallenge: Boolean?
    ): Flowable<Group>

    fun updateGroup(
        group: Group?,
        name: String?,
        description: String?,
        leader: String?,
        leaderCreateChallenge: Boolean?
    ): Flowable<Group>

    fun retrieveGroups(type: String): Flowable<List<Group>>
    fun getGroups(type: String): Flowable<out List<Group>>

    fun getInboxMessages(replyToUserID: String?): Flowable<out List<ChatMessage>>
    fun retrieveInboxMessages(uuid: String, page: Int): Flowable<List<ChatMessage>>
    fun retrieveInboxConversations(): Flowable<List<InboxConversation>>
    fun getInboxConversations(): Flowable<out List<InboxConversation>>
    fun postPrivateMessage(
        recipientId: String,
        messageObject: HashMap<String, String>
    ): Flowable<List<ChatMessage>>

    fun postPrivateMessage(recipientId: String, message: String): Flowable<List<ChatMessage>>

    fun getGroupMembers(id: String): Flow<List<Member>>
    fun retrieveGroupMembers(id: String, includeAllPublicFields: Boolean): Flowable<List<Member>>

    fun inviteToGroup(id: String, inviteData: Map<String, Any>): Flowable<List<Void>>

    fun getMember(userId: String?): Flowable<Member>
    fun getMemberWithUsername(username: String?): Flowable<Member>

    fun findUsernames(
        username: String,
        context: String? = null,
        id: String? = null
    ): Flowable<List<FindUsernameResult>>

    fun markPrivateMessagesRead(user: User?): Flowable<Void>

    fun markSomePrivateMessagesAsRead(user: User?, messages: List<ChatMessage>)

    fun transferGroupOwnership(groupID: String, userID: String): Flowable<Group>
    fun removeMemberFromGroup(groupID: String, userID: String): Flowable<List<Member>>

    fun acceptQuest(user: User?, partyId: String = "party"): Flowable<Void>
    fun rejectQuest(user: User?, partyId: String = "party"): Flowable<Void>

    fun leaveQuest(partyId: String): Flowable<Void>

    fun cancelQuest(partyId: String): Flowable<Void>

    fun abortQuest(partyId: String): Flowable<Quest>

    fun rejectGroupInvite(groupId: String): Flowable<Void>

    fun forceStartQuest(party: Group): Flowable<Quest>

    fun getMemberAchievements(userId: String?): Flowable<List<Achievement>>

    fun transferGems(giftedID: String, amount: Int): Flowable<Void>

    fun getGroupMembership(id: String): Flowable<GroupMembership>
    fun getGroupMemberships(): Flowable<out List<GroupMembership>>
    fun blockMember(userID: String): Flowable<List<String>>
}
