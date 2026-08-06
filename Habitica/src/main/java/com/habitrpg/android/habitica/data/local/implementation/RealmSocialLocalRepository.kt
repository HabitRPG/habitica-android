package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.SocialLocalRepository
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.ChatMessageLike
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.social.GroupMembership
import com.habitrpg.android.habitica.models.social.InboxConversation
import com.habitrpg.android.habitica.models.user.User
import io.realm.Realm
import io.realm.Sort
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class RealmSocialLocalRepository(
    realm: Realm,
) : RealmBaseLocalRepository(realm),
    SocialLocalRepository {
    override fun getGroupMembership(
        userId: String,
        id: String,
    ) = safeFindOne {
        it.where(GroupMembership::class.java)
            .equalTo("userID", userId)
            .equalTo("groupID", id)
    }

    override fun getGroupMemberships(userId: String): Flow<List<GroupMembership>> = safeFindAll {
        it.where(GroupMembership::class.java).equalTo("userID", userId)
    }

    override fun updateMembership(
        userId: String,
        id: String,
        isMember: Boolean,
    ) {
        if (isMember) {
            save(GroupMembership(userId, id))
        } else {
            val membership =
                safeQuery {
                    it.where(GroupMembership::class.java)
                        .equalTo("userID", userId)
                        .equalTo("groupID", id)
                }?.findFirst()
            if (membership != null) {
                executeTransaction {
                    membership.deleteFromRealm()
                }
            }
        }
    }

    override fun saveGroup(group: Group) {
        save(group)
        if (group.quest == null) {
            val existingQuest = safeQuery { it.where(Quest::class.java).equalTo("id", group.id) }?.findFirst()
            executeTransaction {
                existingQuest?.deleteFromRealm()
            }
        }
    }

    override fun saveInboxMessages(
        userID: String,
        recipientID: String,
        messages: List<ChatMessage>,
        page: Int,
    ) {
        messages.forEach { it.userID = userID }
        for (message in messages) {
            val existingMessage =
                safeQuery { it.where(ChatMessage::class.java).equalTo("id", message.id) }?.findFirst()
            message.isSeen = existingMessage != null
        }
        save(messages)
        if (page != 0) return
        val existingMessages =
            safeQuery {
                it.where(ChatMessage::class.java)
                    .equalTo("isInboxMessage", true)
                    .equalTo("uuid", recipientID)
            }?.findAll() ?: return
        val messagesToRemove = ArrayList<ChatMessage>()
        for (existingMessage in existingMessages) {
            val isStillMember = messages.any { existingMessage.id == it.id }
            if (!isStillMember) {
                messagesToRemove.add(existingMessage)
            }
        }
        executeTransaction {
            messagesToRemove.forEach { it.deleteFromRealm() }
        }
    }

    override fun saveInboxConversations(
        userID: String,
        conversations: List<InboxConversation>,
    ) {
        conversations.forEach { it.userID = userID }
        save(conversations)
        val existingConversations =
            safeQuery { it.where(InboxConversation::class.java) }?.findAll() ?: return
        val conversationsToRemove = ArrayList<InboxConversation>()
        for (existingMessage in existingConversations) {
            val isStillMember = conversations.any { existingMessage.uuid == it.uuid }
            if (!isStillMember) {
                conversationsToRemove.add(existingMessage)
            }
        }
        executeTransaction {
            conversationsToRemove.forEach { it.deleteFromRealm() }
        }
    }

    override fun getMember(userID: String?): Flow<Member?> = safeFindOne {
        it.where(Member::class.java).equalTo("id", userID)
    }

    override fun saveGroupMemberships(
        userID: String?,
        memberships: List<GroupMembership>,
    ) {
        save(memberships)
        if (userID != null) {
            val existingMemberships =
                safeQuery { it.where(GroupMembership::class.java).equalTo("userID", userID) }
                    ?.findAll() ?: return
            val membersToRemove = ArrayList<GroupMembership>()
            for (existingMembership in existingMemberships) {
                val isStillMember = memberships.any { existingMembership.groupID == it.groupID }
                if (!isStillMember) {
                    membersToRemove.add(existingMembership)
                }
            }
            executeTransaction {
                membersToRemove.forEach { it.deleteFromRealm() }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getUserGroups(
        userID: String,
        type: String?,
    ) = getGroupMemberships(userID)
        .flatMapLatest { memberships ->
            safeFindAll {
                it.where(Group::class.java)
                    .equalTo("type", type ?: "guild")
                    .`in`(
                        "id",
                        memberships
                            .map {
                                return@map it.groupID
                            }.toTypedArray(),
                    ).sort("memberCount", Sort.DESCENDING)
            }
        }

    override fun getGroup(id: String): Flow<Group?> = safeFindOne {
        it.where(Group::class.java).equalTo("id", id)
    }

    override fun getGroupChat(groupId: String): Flow<List<ChatMessage>> = safeFindAll {
        it.where(ChatMessage::class.java)
            .equalTo("groupId", groupId)
            .sort("timestamp", Sort.DESCENDING)
    }

    override fun deleteMessage(id: String) {
        val chatMessage = safeQuery { it.where(ChatMessage::class.java).equalTo("id", id) }?.findFirst()
        executeTransaction { chatMessage?.deleteFromRealm() }
    }

    override fun getPartyMembers(partyId: String) = safeFindAll {
        it.where(Member::class.java).equalTo("party.id", partyId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getGroupMembers(groupID: String) =
        safeFindAll {
            it.where(GroupMembership::class.java).equalTo("groupID", groupID)
        }
            .map { memberships -> memberships.map { it.userID }.toTypedArray() }
            .flatMapLatest { userIDs ->
                safeFindAll {
                    it.where(Member::class.java).`in`("id", userIDs)
                }
            }

    override fun updateRSVPNeeded(
        user: User?,
        newValue: Boolean,
    ) {
        if (user == null) return
        val visitorUser = getLiveObject(user) ?: return
        executeTransaction { visitorUser.party?.quest?.rsvpNeeded = newValue }
    }

    override fun likeMessage(
        chatMessage: ChatMessage,
        userId: String,
        liked: Boolean,
    ) {
        val liveMessage = getLiveObject(chatMessage)
        if (liveMessage == null) {
            executeTransaction {
                realm.insertOrUpdate(chatMessage)
                return@executeTransaction
            }
            return
        }
        if (liveMessage.userLikesMessage(userId) == liked) {
            return
        }
        if (liked) {
            executeTransaction {
                liveMessage.likes?.add(ChatMessageLike(userId))
                liveMessage.likeCount = liveMessage.likes?.size ?: 0
            }
        } else {
            liveMessage.likes?.filter { userId == it.id && it.isManaged }?.forEach { like ->
                executeTransaction {
                    like.deleteFromRealm()
                }
            }
            executeTransaction {
                liveMessage.likeCount = liveMessage.likes?.size ?: 0
            }
        }
    }

    override fun savePartyMembers(
        groupId: String?,
        members: List<Member>,
    ) {
        save(members)
        if (groupId != null) {
            val existingMembers =
                safeQuery { it.where(Member::class.java).equalTo("party.id", groupId) }?.findAll()
                    ?: return
            val membersToRemove = ArrayList<Member>()
            for (existingMember in existingMembers) {
                val isStillMember =
                    members.any { existingMember.id == it.id }
                if (!isStillMember) {
                    membersToRemove.add(existingMember)
                }
            }
            executeTransaction {
                membersToRemove.forEach { it.deleteFromRealm() }
            }
        }
    }

    override fun rejectGroupInvitation(
        userID: String,
        groupID: String,
    ) {
        val user = safeQuery { it.where(User::class.java).equalTo("id", userID) }?.findFirst()
        executeTransaction {
            user?.invitations?.removeInvitation(groupID)
        }
    }

    override fun removeQuest(partyId: String) {
        val party = safeQuery { it.where(Group::class.java).equalTo("id", partyId) }?.findFirst()
        if (party != null) {
            executeTransaction { party.quest = null }
        }
    }

    override fun setQuestActivity(
        party: Group?,
        active: Boolean,
    ) {
        if (party == null) return
        val liveParty = getLiveObject(party)
        executeTransaction {
            liveParty?.quest?.active = active
        }
    }

    override fun saveChatMessages(
        groupId: String?,
        chatMessages: List<ChatMessage>,
    ) {
        save(chatMessages)
        if (groupId != null) {
            val existingMessages =
                safeQuery { it.where(ChatMessage::class.java).equalTo("groupId", groupId) }?.findAll()
                    ?: return
            val messagesToRemove = ArrayList<ChatMessage>()
            for (existingMessage in existingMessages) {
                val isStillMember = chatMessages.any { existingMessage.id == it.id }
                if (!isStillMember) {
                    messagesToRemove.add(existingMessage)
                }
            }
            executeTransaction {
                for (message in messagesToRemove) {
                    message.deleteFromRealm()
                }
            }
        }
    }

    override fun doesGroupExist(id: String): Boolean {
        val party = safeQuery { it.where(Group::class.java).equalTo("id", id) }?.findFirst()
        return party != null && party.isValid
    }

    override fun getInboxMessages(
        userId: String,
        replyToUserID: String?,
    ) = safeFindAll {
        it.where(ChatMessage::class.java)
            .equalTo("isInboxMessage", true)
            .equalTo("uuid", replyToUserID)
            .equalTo("userID", userId)
            .sort("timestamp", Sort.DESCENDING)
    }

    override fun getInboxConversation(userId: String) = safeFindAll {
        it.where(InboxConversation::class.java)
            .equalTo("userID", userId)
            .sort("timestamp", Sort.DESCENDING)
    }
}
