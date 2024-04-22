package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.invitations.InviteResponse
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.responses.PostChatMessageResult
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.FindUsernameResult
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.social.GroupMembership
import com.habitrpg.android.habitica.models.social.InboxConversation
import com.habitrpg.android.habitica.models.user.User
import io.realm.RealmResults
import kotlinx.coroutines.flow.Flow

interface SocialRepository : BaseRepository {
    fun getUserGroups(type: String?): Flow<List<Group>>

    suspend fun retrieveGroupChat(groupId: String): List<ChatMessage>?

    fun getGroupChat(groupId: String): Flow<List<ChatMessage>>

    suspend fun markMessagesSeen(seenGroupId: String)

    suspend fun flagMessage(
        chatMessageID: String,
        additionalInfo: String,
        groupID: String? = null,
    ): Void?

    suspend fun reportMember(
        memberID: String,
        data: Map<String, String>,
    ): Void?

    suspend fun likeMessage(chatMessage: ChatMessage): ChatMessage?

    suspend fun deleteMessage(chatMessage: ChatMessage): Void?

    suspend fun postGroupChat(
        groupId: String,
        messageObject: HashMap<String, String>,
    ): PostChatMessageResult?

    suspend fun postGroupChat(
        groupId: String,
        message: String,
    ): PostChatMessageResult?

    suspend fun retrieveGroup(id: String): Group?

    fun getGroup(id: String?): Flow<Group?>

    suspend fun leaveGroup(
        id: String?,
        keepChallenges: Boolean,
    ): Group?

    suspend fun joinGroup(id: String?): Group?

    suspend fun createGroup(
        name: String?,
        description: String?,
        leader: String?,
        type: String?,
        privacy: String?,
        leaderCreateChallenge: Boolean?,
    ): Group?

    suspend fun updateGroup(
        group: Group?,
        name: String?,
        description: String?,
        leader: String?,
        leaderCreateChallenge: Boolean?,
    ): Group?

    fun getInboxMessages(replyToUserID: String?): Flow<RealmResults<ChatMessage>>

    suspend fun retrieveInboxMessages(
        uuid: String,
        page: Int,
    ): List<ChatMessage>?

    suspend fun retrieveInboxConversations(): List<InboxConversation>?

    fun getInboxConversations(): Flow<RealmResults<InboxConversation>>

    suspend fun postPrivateMessage(
        recipientId: String,
        messageObject: HashMap<String, String>,
    ): List<ChatMessage>?

    suspend fun postPrivateMessage(
        recipientId: String,
        message: String,
    ): List<ChatMessage>?

    suspend fun getPartyMembers(id: String): Flow<List<Member>>

    suspend fun getGroupMembers(id: String): Flow<List<Member>>

    suspend fun retrievePartyMembers(
        id: String,
        includeAllPublicFields: Boolean,
    ): List<Member>?

    suspend fun inviteToGroup(
        id: String,
        inviteData: Map<String, Any>,
    ): List<InviteResponse>?

    suspend fun retrieveMember(
        userId: String?,
        fromHall: Boolean = false,
    ): Member?

    suspend fun findUsernames(
        username: String,
        context: String? = null,
        id: String? = null,
    ): List<FindUsernameResult>?

    suspend fun markPrivateMessagesRead(user: User?)

    fun markSomePrivateMessagesAsRead(
        user: User?,
        messages: List<ChatMessage>,
    )

    suspend fun transferGroupOwnership(
        groupID: String,
        userID: String,
    ): Group?

    suspend fun removeMemberFromGroup(
        groupID: String,
        userID: String,
    ): List<Member>?

    suspend fun acceptQuest(
        user: User?,
        partyId: String = "party",
    ): Void?

    suspend fun rejectQuest(
        user: User?,
        partyId: String = "party",
    ): Void?

    suspend fun leaveQuest(partyId: String): Void?

    suspend fun cancelQuest(partyId: String): Void?

    suspend fun abortQuest(partyId: String): Quest?

    suspend fun rejectGroupInvite(groupId: String): Void?

    suspend fun forceStartQuest(party: Group): Quest?

    suspend fun getMemberAchievements(userId: String?): List<Achievement>?

    suspend fun transferGems(
        giftedID: String,
        amount: Int,
    ): Void?

    fun getGroupMembership(id: String): Flow<GroupMembership?>

    fun getGroupMemberships(): Flow<List<GroupMembership>>

    suspend fun blockMember(userID: String): List<String>?

    fun getMember(userID: String?): Flow<Member?>

    suspend fun updateMember(
        memberID: String,
        data: Map<String, Map<String, Boolean>>,
    ): Member?

    suspend fun retrievePartySeekingUsers(page: Int = 0): List<Member>?

    suspend fun retrievegroupInvites(
        id: String,
        includeAllPublicFields: Boolean,
    ): List<Member>?
}
