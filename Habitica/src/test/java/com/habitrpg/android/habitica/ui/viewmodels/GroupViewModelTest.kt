package com.habitrpg.android.habitica.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.NotificationsManager
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.responses.PostChatMessageResult
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.social.GroupMembership
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.views.LoadingButtonState
import com.habitrpg.common.habitica.models.Notification
import com.habitrpg.common.habitica.models.notifications.NewChatMessageData
import com.habitrpg.common.habitica.models.notifications.NotificationGroup
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class GroupViewModelTest :
    WordSpec({
        val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
        val userRepository = mockk<UserRepository>(relaxed = true)
        val mainUserViewModel = mockk<MainUserViewModel>()
        val challengeRepository = mockk<ChallengeRepository>(relaxed = true)
        val socialRepository = mockk<SocialRepository>(relaxed = true)
        val notificationsManager = mockk<NotificationsManager>()

        every { mainUserViewModel.user } returns MutableLiveData(User().apply { id = "user-1" })
        every { notificationsManager.getNotifications() } returns flowOf(emptyList())

        fun makeViewModel(groupID: String? = null) =
            GroupViewModel(userRepository, mainUserViewModel, challengeRepository, socialRepository, notificationsManager).apply {
                groupID?.let { setGroupID(it) }
            }

        fun message(
            id: String,
            timestamp: Long,
        ) = ChatMessage().apply {
            this.id = id
            this.timestamp = timestamp
        }

        suspend fun GroupViewModel.loadChat(vararg messages: ChatMessage) {
            coEvery { socialRepository.retrieveGroupChat(any(), 50, null) } returns messages.toList()
            retrieveGroupChat { }
            chatmessages.getOrAwaitValue()
        }

        beforeEach { Dispatchers.setMain(testDispatcher) }
        afterEach { clearMocks(userRepository, challengeRepository, socialRepository, answers = false) }

        "setGroupID" should {
            "read chat notifications for that group" {
                every { notificationsManager.getNotifications() } returns
                    flowOf(
                        listOf(
                            Notification().apply {
                                id = "n1"
                                data = NewChatMessageData().apply { group = NotificationGroup().apply { id = "group-1" } }
                            },
                            Notification().apply {
                                id = "n2"
                                data = NewChatMessageData().apply { group = NotificationGroup().apply { id = "group-2" } }
                            },
                        ),
                    )
                makeViewModel("group-1")
                coVerify(exactly = 1) { userRepository.readNotification("n1") }
                coVerify(exactly = 0) { userRepository.readNotification("n2") }
                every { notificationsManager.getNotifications() } returns flowOf(emptyList())
            }

            "ignore setting the same group twice" {
                val viewModel = makeViewModel("group-1")
                viewModel.setGroupID("group-1")
                viewModel.groupID shouldBe "group-1"
            }
        }

        "group data" should {
            "expose leader and privacy of the group" {
                every { socialRepository.getGroup("group-1") } returns
                    flowOf(
                        Group().apply {
                            id = "group-1"
                            leaderID = "user-1"
                            privacy = "public"
                        },
                    )
                val viewModel = makeViewModel("group-1")
                viewModel.getGroupData().getOrAwaitValue()
                viewModel.leaderID shouldBe "user-1"
                viewModel.isLeader shouldBe true
                viewModel.isPublicGuild shouldBe true
            }

            "report membership based on the stored membership" {
                every { socialRepository.getGroupMembership("group-1") } returns flowOf(GroupMembership("user-1", "group-1"))
                val viewModel = makeViewModel("group-1")
                viewModel.getIsMemberData().getOrAwaitValue() shouldBe true
                viewModel.isMember shouldBe true
            }
        }

        "retrieveGroupChat" should {
            "merge new messages with existing ones sorted by time" {
                val viewModel = makeViewModel("group-1")
                viewModel.loadChat(message("b", 2), message("a", 1))
                coEvery { socialRepository.retrieveGroupChat("group-1", 50, null) } returns listOf(message("c", 3), message("b", 2))
                var hasNewMessages = false
                viewModel.retrieveGroupChat { hasNewMessages = it }
                hasNewMessages shouldBe true
                viewModel.chatmessages.getOrAwaitValue().map { it.id } shouldBe listOf("c", "b", "a")
            }

            "use the party id for parties" {
                val viewModel = makeViewModel("group-1")
                viewModel.groupViewType = GroupViewType.PARTY
                viewModel.retrieveGroupChat { }
                coVerify(exactly = 1) { socialRepository.retrieveGroupChat("party", 50, null) }
            }

            "complete without new messages if there is no group" {
                var hasNewMessages: Boolean? = null
                makeViewModel().retrieveGroupChat { hasNewMessages = it }
                hasNewMessages shouldBe false
                coVerify(exactly = 0) { socialRepository.retrieveGroupChat(any(), any(), any()) }
            }
        }

        "loadOlderMessages" should {
            "append older messages that are not loaded yet" {
                val viewModel = makeViewModel("group-1")
                viewModel.loadChat(message("b", 2))
                coEvery { socialRepository.retrieveGroupChat("group-1", 50, "b") } returns listOf(message("b", 2), message("a", 1))
                var completed = false
                viewModel.loadOlderMessages { completed = true }
                completed shouldBe true
                viewModel.chatmessages.getOrAwaitValue().map { it.id } shouldBe listOf("b", "a")
            }

            "not load anything if there are no messages yet" {
                makeViewModel("group-1").loadOlderMessages { }
                coVerify(exactly = 0) { socialRepository.retrieveGroupChat(any(), any(), any()) }
            }
        }

        "likeMessage" should {
            "replace the liked message" {
                val viewModel = makeViewModel("group-1")
                viewModel.loadChat(message("a", 1))
                val liked = message("a", 1).apply { likeCount = 1 }
                coEvery { socialRepository.likeMessage(any()) } returns liked
                viewModel.likeMessage(message("a", 1))
                viewModel.chatmessages.getOrAwaitValue().single().likeCount shouldBe 1
            }

            "reload the chat if liking failed" {
                val viewModel = makeViewModel("group-1")
                coEvery { socialRepository.likeMessage(any()) } returns null
                viewModel.likeMessage(message("a", 1))
                coVerify(exactly = 1) { socialRepository.retrieveGroupChat("group-1", 50, null) }
            }
        }

        "deleteMessage" should {
            "remove the message" {
                val viewModel = makeViewModel("group-1")
                viewModel.loadChat(message("b", 2), message("a", 1))
                viewModel.deleteMessage(message("b", 2))
                viewModel.chatmessages.getOrAwaitValue().map { it.id } shouldBe listOf("a")
                coVerify(exactly = 1) { socialRepository.deleteMessage(any()) }
            }

            "restore the message if deleting failed" {
                val viewModel = makeViewModel("group-1")
                viewModel.loadChat(message("c", 3), message("a", 1))
                coEvery { socialRepository.deleteMessage(any()) } throws IllegalStateException()
                viewModel.deleteMessage(message("b", 2))
                viewModel.chatmessages.getOrAwaitValue().map { it.id } shouldBe listOf("c", "b", "a")
            }
        }

        "postGroupChat" should {
            "add the posted message to the top" {
                val viewModel = makeViewModel("group-1")
                viewModel.loadChat(message("a", 1))
                coEvery { socialRepository.postGroupChat("group-1", "Hello") } returns PostChatMessageResult().apply { message = message("b", 2) }
                var completed = false
                viewModel.postGroupChat("Hello", { completed = true }, { })
                completed shouldBe true
                viewModel.chatmessages.getOrAwaitValue().map { it.id } shouldBe listOf("b", "a")
            }

            "call the error handler if posting failed" {
                coEvery { socialRepository.postGroupChat("group-1", "Hello") } throws IllegalStateException()
                var failed = false
                makeViewModel("group-1").postGroupChat("Hello", { }, { failed = true })
                failed shouldBe true
            }
        }

        "markMessagesSeen" should {
            "only mark messages seen if there are new messages" {
                val viewModel = makeViewModel("group-1")
                viewModel.markMessagesSeen()
                coVerify(exactly = 0) { socialRepository.markMessagesSeen(any()) }
                viewModel.gotNewMessages = true
                viewModel.markMessagesSeen()
                coVerify(exactly = 1) { socialRepository.markMessagesSeen("group-1") }
            }
        }

        "leaveGroup" should {
            "leave challenges and abort the quest before leaving" {
                val challenge = Challenge()
                var completed = false
                makeViewModel("group-1").leaveGroup(false, true, listOf(challenge), keepChallenges = false) { completed = true }
                coVerify(exactly = 1) { challengeRepository.leaveChallenge(challenge, "remove-all") }
                coVerify(exactly = 1) { socialRepository.abortQuest("group-1") }
                coVerify(exactly = 1) { socialRepository.leaveGroup("group-1", false) }
                coVerify(exactly = 1) { userRepository.retrieveUser(false, true) }
                completed shouldBe true
            }

            "leave the quest and keep challenges" {
                makeViewModel("group-1").leaveGroup(true, false, listOf(Challenge()))
                coVerify(exactly = 0) { challengeRepository.leaveChallenge(any(), any()) }
                coVerify(exactly = 1) { socialRepository.leaveQuest("group-1") }
                coVerify(exactly = 1) { socialRepository.leaveGroup("group-1", true) }
            }
        }

        "joinGroup" should {
            "join the current group if no id is given" {
                var completed = false
                makeViewModel("group-1").joinGroup { completed = true }
                coVerify(exactly = 1) { socialRepository.joinGroup("group-1") }
                completed shouldBe true
            }
        }

        "rejectGroupInvite" should {
            "reject the given group" {
                makeViewModel("group-1").rejectGroupInvite("group-2")
                coVerify(exactly = 1) { socialRepository.rejectGroupInvite("group-2") }
            }
        }

        "retrieveGroup" should {
            "load party members and pending invites for the party leader" {
                every { socialRepository.getGroup("party-1") } returns flowOf(Group().apply { id = "party-1"; leaderID = "user-1" })
                coEvery { socialRepository.retrieveGroup("party-1") } returns Group().apply { id = "party-1" }
                val invited = Member().apply { id = "member-2" }
                coEvery { socialRepository.retrievegroupInvites("party-1", true) } returns listOf(invited)
                val viewModel = makeViewModel("party-1")
                viewModel.groupViewType = GroupViewType.PARTY
                viewModel.getGroupData().getOrAwaitValue()
                var completed = false
                viewModel.retrieveGroup { completed = true }
                coVerify(exactly = 1) { socialRepository.retrievePartyMembers("party-1", true) }
                viewModel.pendingInvites.toList() shouldBe listOf(invited)
                completed shouldBe true
            }
        }

        "rescindInvite" should {
            "mark the invite as rescinded" {
                val invited = Member().apply { id = "member-2" }
                val viewModel = makeViewModel("group-1")
                viewModel.rescindInvite(invited)
                coVerify(exactly = 1) { socialRepository.removeMemberFromGroup("group-1", "member-2") }
                viewModel.pendingInviteStates["member-2"] shouldBe LoadingButtonState.SUCCESS
            }

            "mark the invite as failed if removing failed" {
                coEvery { socialRepository.removeMemberFromGroup(any(), any()) } throws IllegalStateException()
                val viewModel = makeViewModel("group-1")
                viewModel.rescindInvite(Member().apply { id = "member-2" })
                viewModel.pendingInviteStates["member-2"] shouldBe LoadingButtonState.FAILED
            }
        }
    })
