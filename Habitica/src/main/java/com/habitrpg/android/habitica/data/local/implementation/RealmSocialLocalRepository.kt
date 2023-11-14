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
import io.realm.kotlin.toFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class RealmSocialLocalRepository(realm: Realm) : RealmBaseLocalRepository(realm), SocialLocalRepository {

    override fun getGroupMembership(userId: String, id: String) = realm.where(GroupMembership::class.java)
        .equalTo("userID", userId)
        .equalTo("groupID", id)
        .findAll()
        .toFlow()
        .filter { it.isLoaded && it.isNotEmpty() }
        .map { it.first() }

    override fun getGroupMemberships(userId: String): Flow<List<GroupMembership>> = realm.where(GroupMembership::class.java)
        .equalTo("userID", userId)
        .findAll()
        .toFlow()
        .filter { it.isLoaded }

    override fun updateMembership(userId: String, id: String, isMember: Boolean) {
        if (isMember) {
            save(GroupMembership(userId, id))
        } else {
            val membership = realm.where(GroupMembership::class.java).equalTo("userID", userId).equalTo("groupID", id).findFirst()
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
            val existingQuest = realm.where(Quest::class.java).equalTo("id", group.id).findFirst()
            executeTransaction {
                existingQuest?.deleteFromRealm()
            }
        }
    }

    override fun saveInboxMessages(
        userID: String,
        recipientID: String,
        messages: List<ChatMessage>,
        page: Int
    ) {
        messages.forEach { it.userID = userID }
        for (message in messages) {
            val existingMessage = realm.where(ChatMessage::class.java)
                .equalTo("id", message.id)
                .findAll()
                .firstOrNull()
            message.isSeen = existingMessage != null
        }
        save(messages)
        if (page != 0) return
        val existingMessages = realm.where(ChatMessage::class.java).equalTo("isInboxMessage", true).equalTo("uuid", recipientID).findAll()
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

    override fun saveInboxConversations(userID: String, conversations: List<InboxConversation>) {
        conversations.forEach { it.userID = userID }
        save(conversations)
        val existingConversations = realm.where(InboxConversation::class.java).findAll()
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

    override fun getMember(userID: String?): Flow<Member?> {
        return realm.where(Member::class.java)
            .equalTo("id", userID)
            .findAll()
            .toFlow()
            .filter { member -> member.isLoaded && member.isValid }
            .map { member -> member.firstOrNull() }
    }

    override fun saveGroupMemberships(userID: String?, memberships: List<GroupMembership>) {
        save(memberships)
        if (userID != null) {
            val existingMemberships = realm.where(GroupMembership::class.java).equalTo("userID", userID).findAll()
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
    override fun getUserGroups(userID: String, type: String?) = realm.where(GroupMembership::class.java)
        .equalTo("userID", userID)
        .findAll()
        .toFlow()
        .filter { it.isLoaded }
        .flatMapLatest { memberships ->
            realm.where(Group::class.java)
                .equalTo("type", type ?: "guild")
                .`in`(
                    "id",
                    memberships.map {
                        return@map it.groupID
                    }.toTypedArray()
                )
                .sort("memberCount", Sort.DESCENDING)
                .findAll()
                .toFlow()
        }

    override fun getGroup(id: String): Flow<Group?> {
        return realm.where(Group::class.java)
            .equalTo("id", id)
            .findAll()
            .toFlow()
            .filter { group -> group.isLoaded && group.isValid && !group.isEmpty() }
            .map { groups -> groups.first() }
    }

    override fun getGroupChat(groupId: String): Flow<List<ChatMessage>> {
        return realm.where(ChatMessage::class.java)
            .equalTo("groupId", groupId)
            .sort("timestamp", Sort.DESCENDING)
            .findAll()
            .toFlow()
            .filter { it.isLoaded }
    }

    override fun deleteMessage(id: String) {
        val chatMessage = realm.where(ChatMessage::class.java).equalTo("id", id).findFirst()
        executeTransaction { chatMessage?.deleteFromRealm() }
    }

    override fun getPartyMembers(partyId: String) = realm.where(Member::class.java)
        .equalTo("party.id", partyId)
        .findAll()
        .toFlow()

    override fun getGroupMembers(groupID: String) = realm.where(GroupMembership::class.java)
        .equalTo("groupID", groupID)
        .findAll()
        .toFlow()
        .map { memberships -> memberships.map { it.userID }.toTypedArray() }
        .flatMapLatest { realm.where(Member::class.java).`in`("id", it).findAll().toFlow() }

    override fun updateRSVPNeeded(user: User?, newValue: Boolean) {
        executeTransaction { user?.party?.quest?.RSVPNeeded = newValue }
    }

    override fun likeMessage(chatMessage: ChatMessage, userId: String, liked: Boolean) {
        if (chatMessage.userLikesMessage(userId) == liked) {
            return
        }
        val liveMessage = getLiveObject(chatMessage)
        if (liked) {
            executeTransaction {
                liveMessage?.likes?.add(ChatMessageLike(userId))
                liveMessage?.likeCount = liveMessage?.likes?.size ?: 0
            }
        } else {
            liveMessage?.likes?.filter { userId == it.id && it.isManaged }?.forEach { like ->
                executeTransaction {
                    like.deleteFromRealm()
                }
            }
            executeTransaction {
                liveMessage?.likeCount = liveMessage?.likes?.size ?: 0
            }
        }
    }

    override fun savePartyMembers(groupId: String?, members: List<Member>) {
        save(members)
        if (groupId != null) {
            val existingMembers = realm.where(Member::class.java).equalTo("party.id", groupId).findAll()
            val membersToRemove = ArrayList<Member>()
            for (existingMember in existingMembers) {
                val isStillMember = members.any { existingMember.id != null && existingMember.id == it.id }
                if (!isStillMember) {
                    membersToRemove.add(existingMember)
                }
            }
            executeTransaction {
                membersToRemove.forEach { it.deleteFromRealm() }
            }
        }
    }

    override fun rejectGroupInvitation(userID: String, groupID: String) {
        val user = realm.where(User::class.java).equalTo("id", userID).findFirst()
        executeTransaction {
            user?.invitations?.removeInvitation(groupID)
        }
    }

    override fun removeQuest(partyId: String) {
        val party = realm.where(Group::class.java).equalTo("id", partyId).findFirst()
        if (party != null) {
            executeTransaction { party.quest = null }
        }
    }

    override fun setQuestActivity(party: Group?, active: Boolean) {
        if (party == null) return
        val liveParty = getLiveObject(party)
        executeTransaction {
            liveParty?.quest?.active = active
        }
    }

    override fun saveChatMessages(groupId: String?, chatMessages: List<ChatMessage>) {
        save(chatMessages)
        if (groupId != null) {
            val existingMessages = realm.where(ChatMessage::class.java).equalTo("groupId", groupId).findAll()
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
        val party = realm.where(Group::class.java).equalTo("id", id).findFirst()
        return party != null && party.isValid
    }

    override fun getInboxMessages(userId: String, replyToUserID: String?) = realm.where(ChatMessage::class.java)
        .equalTo("isInboxMessage", true)
        .equalTo("uuid", replyToUserID)
        .equalTo("userID", userId)
        .sort("timestamp", Sort.DESCENDING)
        .findAll()
        .toFlow()
        .filter { it.isLoaded }

    override fun getInboxConversation(userId: String) = realm.where(InboxConversation::class.java)
        .equalTo("userID", userId)
        .sort("timestamp", Sort.DESCENDING)
        .findAll()
        .toFlow()
        .filter { it.isLoaded }
}
