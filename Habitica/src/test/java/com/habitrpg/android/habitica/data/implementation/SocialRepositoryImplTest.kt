package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.local.SocialLocalRepository
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
                val declaredKnown = Member().apply { id = "member-1"; participatesInQuest = true }
                val declaredNew = Member().apply { id = "member-3"; participatesInQuest = false }
                val group =
                    Group().apply {
                        id = "group-1"
                        quest = Quest().apply { participants?.add(declaredKnown); participants?.add(declaredNew) }
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
                val message = ChatMessage().apply { id = "msg-1"; isInboxMessage = true }
                coEvery { apiClient.deleteInboxMessage("msg-1") } returns null
                every { localRepository.deleteMessage("msg-1") } returns Unit
                repository.deleteMessage(message)
                coVerify { apiClient.deleteInboxMessage("msg-1") }
                coVerify(exactly = 0) { apiClient.deleteMessage(any(), any()) }
                verify { localRepository.deleteMessage("msg-1") }
            }

            "delete via the group endpoint for group messages" {
                val message = ChatMessage().apply { id = "msg-1"; groupId = "group-1"; isInboxMessage = false }
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
                val message = ChatMessage().apply { id = "msg-1"; groupId = "group-1" }
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
