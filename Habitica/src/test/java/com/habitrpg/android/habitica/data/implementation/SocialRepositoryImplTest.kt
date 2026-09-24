package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.local.SocialLocalRepository
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.responses.PostChatMessageResult
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.social.InboxConversation
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

class SocialRepositoryImplTest :
    WordSpec({
        lateinit var repository: SocialRepository
        val localRepository = mockk<SocialLocalRepository>()
        val apiClient = mockk<ApiClient>()
        val authenticationHandler = mockk<AuthenticationHandler>()
        beforeEach {
            every { authenticationHandler.currentUserID } returns "user-1"
            repository = SocialRepositoryImpl(localRepository, apiClient, authenticationHandler)
        }
        afterEach { clearAllMocks() }
        "getGroup" should {
            "return an empty flow for a blank id" {
                repository.getGroup(null).collect { }
                repository.getGroup("").collect { }
                verify(exactly = 0) { localRepository.getGroup(any()) }
            }

            "delegate to the local repository for a real id" {
                val group = Group()
                every { localRepository.getGroup("group-1") } returns flowOf(group)
                repository.getGroup("group-1").collect { it shouldBe group }
            }
        }
        "retrieveGroup" should {
            "save the group returned by the API and fetch its chat" {
                val group = Group().apply { id = "group-1" }
                coEvery { apiClient.getGroup("group-1") } returns group
                every { localRepository.saveGroup(group) } returns Unit
                coEvery { apiClient.listGroupChat("group-1", 50, null) } returns null
                val result = repository.retrieveGroup("group-1")
                result shouldBe group
            }

            "reconcile declared quest participants against the full local party roster" {
                val declaredKnown =
                    Member().apply {
                        id = "member-1"
                        participatesInQuest = true
                    }
                val declaredNew =
                    Member().apply {
                        id = "member-3"
                        participatesInQuest = false
                    }
                val group =
                    Group().apply {
                        id = "group-1"
                        quest =
                            Quest().apply {
                                participants?.add(declaredKnown)
                                participants?.add(declaredNew)
                            }
                    }
                val localMember1 = Member().apply { id = "member-1" }
                val localMember2 = Member().apply { id = "member-2" }
                coEvery { apiClient.getGroup("group-1") } returns group
                every { localRepository.saveGroup(group) } returns Unit
                coEvery { apiClient.listGroupChat("group-1", 50, null) } returns null
                every { localRepository.getPartyMembers("group-1") } returns flowOf(listOf(localMember1, localMember2))
                repository.retrieveGroup("group-1")
                val participants = group.quest?.participants.orEmpty()
                participants.firstOrNull { it.id == "member-1" }?.participatesInQuest shouldBe true
                participants.firstOrNull { it.id == "member-2" }?.participatesInQuest shouldBe null
                participants.firstOrNull { it.id == "member-3" }?.participatesInQuest shouldBe false
                verify { localRepository.saveGroup(group) }
            }
        }
        "leaveGroup" should {
            "do nothing and return null for a blank id" {
                val result = repository.leaveGroup(null, false)
                result shouldBe null
                coVerify(exactly = 0) { apiClient.leaveGroup(any(), any()) }
            }

            "leave remotely, update membership, and return the local group" {
                val group = Group().apply { id = "group-1" }
                coEvery { apiClient.leaveGroup("group-1", "leave-challenges") } returns null
                every { localRepository.updateMembership("user-1", "group-1", false) } returns Unit
                every { localRepository.getGroup("group-1") } returns flowOf(group)
                val result = repository.leaveGroup("group-1", false)
                result shouldBe group
                coVerify { apiClient.leaveGroup("group-1", "leave-challenges") }
                verify { localRepository.updateMembership("user-1", "group-1", false) }
            }

            "keep challenges when requested" {
                coEvery { apiClient.leaveGroup("group-1", "remain-in-challenges") } returns null
                every { localRepository.updateMembership(any(), any(), any()) } returns Unit
                every { localRepository.getGroup("group-1") } returns flowOf(null)
                repository.leaveGroup("group-1", true)
                coVerify { apiClient.leaveGroup("group-1", "remain-in-challenges") }
            }
        }
        "joinGroup" should {
            "do nothing and return null for a blank id" {
                repository.joinGroup(null) shouldBe null
                coVerify(exactly = 0) { apiClient.joinGroup(any()) }
            }

            "update membership and save the group on success" {
                val group = Group().apply { id = "group-1" }
                coEvery { apiClient.joinGroup("group-1") } returns group
                every { localRepository.updateMembership("user-1", "group-1", true) } returns Unit
                every { localRepository.save(group) } returns Unit
                val result = repository.joinGroup("group-1")
                result shouldBe group
                verify { localRepository.updateMembership("user-1", "group-1", true) }
                verify { localRepository.save(group) }
            }
        }
        "deleteMessage" should {
            "delete via the inbox endpoint for inbox messages" {
                val message =
                    ChatMessage().apply {
                        id = "msg-1"
                        isInboxMessage = true
                    }
                coEvery { apiClient.deleteInboxMessage("msg-1") } returns null
                every { localRepository.deleteMessage("msg-1") } returns Unit
                repository.deleteMessage(message)
                coVerify { apiClient.deleteInboxMessage("msg-1") }
                coVerify(exactly = 0) { apiClient.deleteMessage(any(), any()) }
                verify { localRepository.deleteMessage("msg-1") }
            }

            "delete via the group endpoint for group messages" {
                val message =
                    ChatMessage().apply {
                        id = "msg-1"
                        groupId = "group-1"
                        isInboxMessage = false
                    }
                coEvery { apiClient.deleteMessage("group-1", "msg-1") } returns null
                every { localRepository.deleteMessage("msg-1") } returns Unit
                repository.deleteMessage(message)
                coVerify { apiClient.deleteMessage("group-1", "msg-1") }
            }
        }
        "likeMessage" should {
            "return null for a blank message id" {
                val message = ChatMessage().apply { id = "" }
                repository.likeMessage(message) shouldBe null
                coVerify(exactly = 0) { apiClient.likeMessage(any(), any()) }
            }

            "like the message and save the response" {
                val message =
                    ChatMessage().apply {
                        id = "msg-1"
                        groupId = "group-1"
                    }
                val liked = ChatMessage().apply { id = "msg-1" }
                coEvery { apiClient.likeMessage("group-1", "msg-1") } returns liked
                every { localRepository.save(liked) } returns Unit
                val result = repository.likeMessage(message)
                result shouldBe liked
                result?.groupId shouldBe "group-1"
                verify { localRepository.save(liked) }
            }
        }
        "cancelQuest" should {
            "cancel remotely and remove the local quest" {
                coEvery { apiClient.cancelQuest("party-1") } returns null
                every { localRepository.removeQuest("party-1") } returns Unit
                repository.cancelQuest("party-1")
                coVerify { apiClient.cancelQuest("party-1") }
                verify { localRepository.removeQuest("party-1") }
            }
        }
        "transferGroupOwnership" should {
            "update the group with the new leader" {
                val group = Group().apply { id = "group-1" }
                every { localRepository.getGroup("group-1") } returns flowOf(group)
                every { localRepository.getUnmanagedCopy(group) } returns group
                coEvery { apiClient.updateGroup("group-1", group) } returns group
                repository.transferGroupOwnership("group-1", "user-2") shouldBe group
                group.leaderID shouldBe "user-2"
            }
        }
        "removeMemberFromGroup" should {
            "reload the party members afterwards" {
                val members = listOf(Member())
                coEvery { apiClient.removeMemberFromGroup("group-1", "user-2") } returns null
                coEvery { apiClient.getGroupMembers("group-1", true) } returns members
                every { localRepository.savePartyMembers("group-1", members) } returns Unit
                repository.removeMemberFromGroup("group-1", "user-2") shouldBe members
                verify { localRepository.savePartyMembers("group-1", members) }
            }
        }
        "retrieveGroupChat" should {
            "assign the group to all messages" {
                val messages = listOf(ChatMessage(), ChatMessage())
                coEvery { apiClient.listGroupChat("group-1", 50, null) } returns messages
                repository.retrieveGroupChat("group-1", 50, null)?.map { it.groupId } shouldBe listOf("group-1", "group-1")
            }
        }
        "flagMessage" should {
            "not flag messages without id" {
                repository.flagMessage("", "spam", "group-1") shouldBe null
                coVerify(exactly = 0) { apiClient.flagMessage(any(), any(), any()) }
            }

            "flag inbox messages without group" {
                coEvery { apiClient.flagInboxMessage("message-1", mutableMapOf("comment" to "spam")) } returns null
                repository.flagMessage("message-1", "spam", null)
                coVerify { apiClient.flagInboxMessage("message-1", mutableMapOf("comment" to "spam")) }
            }

            "flag group messages in their group" {
                coEvery { apiClient.flagMessage("group-1", "message-1", mutableMapOf("comment" to "spam")) } returns null
                repository.flagMessage("message-1", "spam", "group-1")
                coVerify { apiClient.flagMessage("group-1", "message-1", mutableMapOf("comment" to "spam")) }
            }

            "not flag messages for the testing account" {
                every { authenticationHandler.currentUserID } returns BuildConfig.ANDROID_TESTING_UUID
                repository.flagMessage("message-1", "spam", "group-1") shouldBe null
                coVerify(exactly = 0) { apiClient.flagMessage(any(), any(), any()) }
            }
        }
        "postGroupChat" should {
            "post the message and assign the group" {
                val result = PostChatMessageResult().apply { message = ChatMessage() }
                coEvery { apiClient.postGroupChat("group-1", mapOf("message" to "Hello")) } returns result
                repository.postGroupChat("group-1", "Hello")?.message?.groupId shouldBe "group-1"
            }
        }
        "createGroup" should {
            "create and save the group" {
                val saved = Group().apply { id = "group-1" }
                coEvery { apiClient.createGroup(any()) } returns saved
                every { localRepository.save(saved) } returns Unit
                repository.createGroup("Guild", "Desc", "user-1", "guild", "public", false) shouldBe saved
                coVerify { apiClient.createGroup(match { it.name == "Guild" && it.privacy == "public" && it.leaderID == "user-1" }) }
                verify { localRepository.save(saved) }
            }
        }
        "updateGroup" should {
            "save the changes locally and remotely" {
                val group = Group().apply { id = "group-1" }
                every { localRepository.getUnmanagedCopy(group) } returns group
                every { localRepository.save(group) } returns Unit
                coEvery { apiClient.updateGroup("group-1", group) } returns group
                repository.updateGroup(group, "New", "Desc", "user-2", true) shouldBe group
                group.name shouldBe "New"
                group.leaderID shouldBe "user-2"
                group.leaderOnlyChallenges shouldBe true
            }

            "do nothing without a group" {
                repository.updateGroup(null, "New", null, null, null) shouldBe null
            }
        }
        "inbox" should {
            "mark retrieved messages as inbox messages and save them" {
                val messages = listOf(ChatMessage())
                coEvery { apiClient.retrieveInboxMessages("user-2", 1) } returns messages
                every { localRepository.saveInboxMessages("user-1", "user-2", messages, 1) } returns Unit
                repository.retrieveInboxMessages("user-2", 1) shouldBe messages
                messages.single().isInboxMessage shouldBe true
            }

            "save retrieved conversations" {
                val conversations = listOf(InboxConversation())
                coEvery { apiClient.retrieveInboxConversations() } returns conversations
                every { localRepository.saveInboxConversations("user-1", conversations) } returns Unit
                repository.retrieveInboxConversations() shouldBe conversations
            }

            "post a private message and reload the conversation" {
                coEvery { apiClient.postPrivateMessage(mapOf("message" to "Hi", "toUserId" to "user-2")) } returns null
                coEvery { apiClient.retrieveInboxMessages("user-2", 0) } returns null
                repository.postPrivateMessage("user-2", "Hi") shouldBe null
                coVerify { apiClient.retrieveInboxMessages("user-2", 0) }
            }

            "load conversations for the current user" {
                every { authenticationHandler.userIDFlow } returns flowOf("user-1")
                every { localRepository.getInboxConversation("user-1") } returns flowOf(emptyList())
                repository.getInboxConversations().first() shouldBe emptyList()
            }
        }
        "retrieveMember" should {
            "load members by id" {
                val id = "4f0bd0c5-1d4a-4d48-9e0f-1ab0f0cbd6ba"
                coEvery { apiClient.getMember(id) } returns Member()
                repository.retrieveMember(id, false)
                coVerify { apiClient.getMember(id) }
            }

            "load members by username if the value is no id" {
                coEvery { apiClient.getMemberWithUsername("tester") } returns Member()
                repository.retrieveMember("tester", false)
                coVerify { apiClient.getMemberWithUsername("tester") }
            }

            "load members from the hall" {
                coEvery { apiClient.getHallMember("tester") } returns Member()
                repository.retrieveMember("tester", true)
                coVerify { apiClient.getHallMember("tester") }
            }

            "return null without id" {
                repository.retrieveMember(null, false) shouldBe null
            }
        }
        "quests" should {
            "accept the quest and clear the RSVP" {
                val user = User()
                coEvery { apiClient.acceptQuest("party-1") } returns null
                every { localRepository.updateRSVPNeeded(user, false) } returns Unit
                repository.acceptQuest(user, "party-1")
                verify { localRepository.updateRSVPNeeded(user, false) }
            }

            "reject the quest and clear the RSVP" {
                val user = User()
                coEvery { apiClient.rejectQuest("party-1") } returns null
                every { localRepository.updateRSVPNeeded(user, false) } returns Unit
                repository.rejectQuest(user, "party-1")
                verify { localRepository.updateRSVPNeeded(user, false) }
            }

            "abort the quest and remove it locally" {
                val quest = Quest()
                coEvery { apiClient.abortQuest("party-1") } returns quest
                every { localRepository.removeQuest("party-1") } returns Unit
                repository.abortQuest("party-1") shouldBe quest
            }

            "force start the quest and mark it active" {
                val party = Group().apply { id = "party-1" }
                every { localRepository.getUnmanagedCopy(party) } returns party
                coEvery { apiClient.forceStartQuest("party-1", party) } returns Quest()
                every { localRepository.setQuestActivity(party, true) } returns Unit
                repository.forceStartQuest(party)
                verify { localRepository.setQuestActivity(party, true) }
            }
        }
        "getMemberAchievements" should {
            "return null without id" {
                repository.getMemberAchievements(null) shouldBe null
            }
        }
        "rejectGroupInvite" should {
            "reject remotely and update the local invitation state" {
                coEvery { apiClient.rejectGroupInvite("group-1") } returns null
                every { localRepository.rejectGroupInvitation("user-1", "group-1") } returns Unit
                repository.rejectGroupInvite("group-1")
                coVerify { apiClient.rejectGroupInvite("group-1") }
                verify { localRepository.rejectGroupInvitation("user-1", "group-1") }
            }
        }
    })
