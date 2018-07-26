package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.SocialLocalRepository
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.ChatMessageLike
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.social.GroupMembership
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.Flowable
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import java.util.*


class RealmSocialLocalRepository(realm: Realm) : RealmBaseLocalRepository(realm), SocialLocalRepository {

    override fun getGroupMembership(userId: String, id: String): Flowable<GroupMembership> = realm.where(GroupMembership::class.java)
            .equalTo("userID", userId)
            .equalTo("groupID", id)
            .findAll()
            .asFlowable()
            .filter { it.isLoaded && it.isNotEmpty() }
            .map { it.first() }

    override fun getGroupMemberships(userId: String): Flowable<RealmResults<GroupMembership>> = realm.where(GroupMembership::class.java)
            .equalTo("userID", userId)
            .findAll()
            .asFlowable()
            .filter { it.isLoaded }

    override fun updateMembership(userId: String, id: String, isMember: Boolean) {
        if (isMember) {
            realm.executeTransaction {
                realm.insertOrUpdate(GroupMembership(userId, id))
            }
        } else {
            val membership = realm.where(GroupMembership::class.java).equalTo("userID", userId).equalTo("groupID", id).findFirst()
            if (membership != null) {
                realm.executeTransaction {
                    membership.deleteFromRealm()
                }
            }
        }
    }

    override fun getPublicGuilds(): Flowable<RealmResults<Group>> = realm.where(Group::class.java)
                .equalTo("type", "guild")
                .equalTo("privacy", "public")
                .sort("memberCount", Sort.DESCENDING)
            .findAll()
                .asFlowable()
                .filter { it.isLoaded }

    override fun getUserGroups(userID: String): Flowable<RealmResults<Group>> = realm.where(GroupMembership::class.java)
            .equalTo("userID", userID)
            .findAll()
            .asFlowable()
            .filter { it.isLoaded }
            .flatMap {
                realm.where(Group::class.java)
                        .equalTo("type", "guild")
                        .`in`("id", it.map {
                            return@map it.groupID
                        }.toTypedArray())
                        .sort("memberCount", Sort.DESCENDING)
                        .findAll()
                        .asFlowable()
                        .filter { it.isLoaded }
            }



    override fun getGroups(type: String): Flowable<RealmResults<Group>> {
        return realm.where(Group::class.java)
                .equalTo("type", type)
                .findAllAsync()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun getGroup(id: String): Flowable<Group> {
        return realm.where(Group::class.java)
                .equalTo("id", id)
                .findAll()
                .asFlowable()
                .filter { group -> group.isLoaded && group.isValid && !group.isEmpty() }
                .map { groups -> groups.first() }
    }

    override fun getGroupChat(groupId: String): Flowable<RealmResults<ChatMessage>> {
        return realm.where(ChatMessage::class.java)
                .equalTo("groupId", groupId)
                .sort("timestamp", Sort.DESCENDING)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun deleteMessage(id: String) {
        getMessage(id).firstElement().subscribe { chatMessage -> realm.executeTransaction { chatMessage.deleteFromRealm() } }
    }

    override fun getGroupMembers(partyId: String): Flowable<RealmResults<Member>> {
        return realm.where(Member::class.java)
                .equalTo("party.id", partyId)
                .findAllAsync()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun updateRSVPNeeded(user: User?, newValue: Boolean) {
        realm.executeTransaction { user?.party?.quest?.RSVPNeeded = newValue }
    }

    override fun likeMessage(chatMessage: ChatMessage, userId: String, liked: Boolean) {
        if (chatMessage.userLikesMessage(userId) == liked) {
            return
        }
        if (liked) {
            realm.executeTransaction { chatMessage.likes?.add(ChatMessageLike(userId, chatMessage.id)) }
        } else {
            chatMessage.likes?.filter { userId == it.id }?.forEach { like -> realm.executeTransaction { like.deleteFromRealm() } }
        }
    }

    override fun saveGroupMembers(groupId: String?, members: List<Member>) {
        realm.executeTransaction { realm.insertOrUpdate(members) }
        if (groupId != null) {
            val existingMembers = realm.where(Member::class.java).equalTo("party.id", groupId).findAll()
            val membersToRemove = ArrayList<Member>()
            for (existingMember in existingMembers) {
                val isStillMember = members.any { existingMember.id != null && existingMember.id == it.id }
                if (!isStillMember) {
                    membersToRemove.add(existingMember)
                }
            }
            realm.executeTransaction {
                membersToRemove.forEach { it.deleteFromRealm() }
            }
        }

    }

    override fun removeQuest(partyId: String) {
        val party = realm.where(Group::class.java).equalTo("id", partyId).findFirst()
        if (party != null) {
            realm.executeTransaction { party.quest = null }
        }
    }

    override fun setQuestActivity(party: Group?, active: Boolean) {
        realm.executeTransaction {
            party?.quest?.active = active
        }
    }

    override fun saveChatMessages(groupId: String?, chatMessages: List<ChatMessage>) {
        realm.executeTransaction { realm.insertOrUpdate(chatMessages) }
        if (groupId != null) {
            val existingMessages = realm.where(ChatMessage::class.java).equalTo("groupId", groupId).findAll()
            val messagesToRemove = ArrayList<ChatMessage>()
            for (existingMessage in existingMessages) {
                val isStillMember = chatMessages.any { existingMessage.id == it.id }
                if (!isStillMember) {
                    messagesToRemove.add(existingMessage)
                }
            }
            realm.executeTransaction {
                for (member in messagesToRemove) {
                    member.deleteFromRealm()
                }
            }
        }
    }


    private fun getMessage(id: String): Flowable<ChatMessage> {
        return realm.where(ChatMessage::class.java).equalTo("id", id)
                .findAllAsync()
                .asFlowable()
                .filter { messages -> messages.isLoaded && messages.isValid && !messages.isEmpty() }
                .map { messages -> messages.first() }
    }

    override fun doesGroupExist(id: String): Boolean {
        val party = realm.where(Group::class.java).equalTo("id", id).findFirst()
        return party != null && party.isValid
    }
}
