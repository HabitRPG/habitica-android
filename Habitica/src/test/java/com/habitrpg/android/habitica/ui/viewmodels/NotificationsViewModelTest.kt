package com.habitrpg.android.habitica.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.NotificationsManager
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.invitations.GuildInvite
import com.habitrpg.android.habitica.models.invitations.Invitations
import com.habitrpg.android.habitica.models.invitations.PartyInvite
import com.habitrpg.android.habitica.models.social.UserParty
import com.habitrpg.android.habitica.models.user.Flags
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.common.habitica.models.Notification
import com.habitrpg.common.habitica.models.notifications.NewChatMessageData
import com.habitrpg.common.habitica.models.notifications.NotificationGroup
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkObject
import io.mockk.verify
import io.realm.RealmList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest :
    WordSpec({
        val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
        val userRepository = mockk<UserRepository>(relaxed = true)
        val mainUserViewModel = mockk<MainUserViewModel>()
        val notificationsManager = mockk<NotificationsManager>()
        val socialRepository = mockk<SocialRepository>(relaxed = true)
        val userData = MutableLiveData<User?>()
        val serverNotifications = MutableStateFlow<List<Notification>>(emptyList())

        every { mainUserViewModel.user } returns userData
        every { notificationsManager.getNotifications() } returns serverNotifications
        every { notificationsManager.getNotification(any()) } answers { serverNotifications.value.find { it.id == firstArg() } }

        fun makeViewModel() = NotificationsViewModel(userRepository, mainUserViewModel, notificationsManager, socialRepository)

        fun notification(
            id: String,
            type: Notification.Type,
            seen: Boolean? = null,
        ) = Notification().apply {
            this.id = id
            this.type = type.type
            this.seen = seen
        }

        fun partyMessage(
            id: String,
            groupID: String,
        ) = notification(id, Notification.Type.NEW_CHAT_MESSAGE).apply {
            data = NewChatMessageData().apply { group = NotificationGroup().apply { this.id = groupID } }
        }

        beforeSpec { mockkObject(MainNavigationController) }
        afterSpec { unmockkObject(MainNavigationController) }

        beforeEach {
            Dispatchers.setMain(testDispatcher)
            every { MainNavigationController.navigate(any<Int>(), any()) } just runs
            serverNotifications.value = emptyList()
            userData.value = null
        }
        afterEach { clearMocks(userRepository, socialRepository, MainNavigationController, answers = false) }

        "getNotifications" should {
            "only include supported server notification types sorted by priority" {
                val viewModel = makeViewModel()
                serverNotifications.value =
                    listOf(
                        notification("1", Notification.Type.NEW_CHAT_MESSAGE),
                        notification("2", Notification.Type.LOGIN_INCENTIVE),
                        notification("3", Notification.Type.NEW_STUFF),
                    )
                viewModel.getNotifications().first().map { it.id } shouldBe listOf("3", "1")
            }

            "add invitations from the user" {
                val viewModel = makeViewModel()
                userData.value =
                    User().apply {
                        invitations =
                            Invitations().apply {
                                parties = RealmList(PartyInvite().apply { id = "party-1" })
                                guilds = RealmList(GuildInvite().apply { id = "guild-1" })
                            }
                        party =
                            UserParty().apply {
                                id = "party-2"
                                quest = Quest().apply { rsvpNeeded = true; key = "dilatory" }
                            }
                    }
                viewModel.getNotifications().first().map { it.id } shouldBe
                    listOf("custom-guild-invitation-guild-1", "custom-party-invitation-party-1", "custom-quest-invitation-party-2")
            }

            "add a new stuff notification if the user has unseen news" {
                val viewModel = makeViewModel()
                userData.value = User().apply { flags = Flags().apply { newStuff = true } }
                viewModel.getNotifications().first().map { it.id } shouldBe listOf("custom-new-stuff-notification")
            }

            "prefer the server new stuff notification over the custom one" {
                val viewModel = makeViewModel()
                userData.value = User().apply { flags = Flags().apply { newStuff = true } }
                serverNotifications.value = listOf(notification("server-news", Notification.Type.NEW_STUFF))
                viewModel.getNotifications().first().map { it.id } shouldBe listOf("server-news")
            }
        }

        "getNotificationCount" should {
            "not count unallocated stat points if the user has no class" {
                val viewModel = makeViewModel()
                userData.value = User()
                serverNotifications.value =
                    listOf(
                        notification("1", Notification.Type.UNALLOCATED_STATS_POINTS),
                        notification("2", Notification.Type.NEW_CHAT_MESSAGE),
                    )
                viewModel.getNotificationCount().first() shouldBe 1
            }
        }

        "allNotificationsSeen" should {
            "return true only if every notification was seen" {
                val viewModel = makeViewModel()
                serverNotifications.value = listOf(notification("1", Notification.Type.NEW_CHAT_MESSAGE, seen = true))
                viewModel.allNotificationsSeen().first() shouldBe true
                serverNotifications.value += notification("2", Notification.Type.NEW_STUFF, seen = false)
                viewModel.allNotificationsSeen().first() shouldBe false
            }
        }

        "getHasPartyNotification" should {
            "return true if there is a message in the users party" {
                val viewModel = makeViewModel()
                userData.value = User().apply { party = UserParty().apply { id = "party-1" } }
                serverNotifications.value = listOf(partyMessage("1", "guild-1"))
                viewModel.getHasPartyNotification().first() shouldBe false
                serverNotifications.value = listOf(partyMessage("1", "party-1"))
                viewModel.getHasPartyNotification().first() shouldBe true
            }
        }

        "dismissNotification" should {
            "mark server notifications as read" {
                makeViewModel().dismissNotification(notification("1", Notification.Type.NEW_CHAT_MESSAGE))
                coVerify(exactly = 1) { userRepository.readNotification("1") }
            }

            "clear the new stuff flag for the custom new stuff notification" {
                val viewModel = makeViewModel()
                userData.value = User().apply { flags = Flags().apply { newStuff = true } }
                val news = viewModel.getNotifications().first().single()
                viewModel.dismissNotification(news)
                coVerify(exactly = 1) { userRepository.updateUser("flags.newStuff", false) }
                viewModel.getNotifications().first() shouldBe emptyList()
                coVerify(exactly = 0) { userRepository.readNotification(any()) }
            }

            "do nothing for other custom notifications" {
                makeViewModel().dismissNotification(notification("custom-party-invitation-1", Notification.Type.PARTY_INVITATION))
                coVerify(exactly = 0) { userRepository.readNotification(any()) }
            }
        }

        "dismissAllNotifications" should {
            "read all dismissable server notifications at once" {
                makeViewModel().dismissAllNotifications(
                    listOf(
                        notification("1", Notification.Type.NEW_CHAT_MESSAGE),
                        notification("2", Notification.Type.GUILD_INVITATION),
                        notification("custom-3", Notification.Type.PARTY_INVITATION),
                        notification("4", Notification.Type.NEW_STUFF),
                    ),
                )
                coVerify(exactly = 1) { userRepository.readNotifications(mapOf("notificationIds" to listOf("1", "4"))) }
            }

            "not call the server if nothing can be dismissed" {
                makeViewModel().dismissAllNotifications(listOf(notification("custom-1", Notification.Type.PARTY_INVITATION)))
                coVerify(exactly = 0) { userRepository.readNotifications(any()) }
            }
        }

        "markNotificationsAsSeen" should {
            "only send unseen server notifications" {
                makeViewModel().markNotificationsAsSeen(
                    listOf(
                        notification("1", Notification.Type.NEW_CHAT_MESSAGE, seen = false),
                        notification("2", Notification.Type.NEW_CHAT_MESSAGE, seen = true),
                        notification("custom-3", Notification.Type.PARTY_INVITATION, seen = false),
                    ),
                )
                coVerify(exactly = 1) { userRepository.seeNotifications(mapOf("notificationIds" to listOf("1"))) }
            }

            "not call the server if everything was seen" {
                makeViewModel().markNotificationsAsSeen(listOf(notification("1", Notification.Type.NEW_CHAT_MESSAGE, seen = true)))
                coVerify(exactly = 0) { userRepository.seeNotifications(any()) }
            }
        }

        "click" should {
            "open the news for new stuff notifications" {
                serverNotifications.value = listOf(notification("1", Notification.Type.NEW_STUFF))
                makeViewModel().click("1", MainNavigationController)
                verify { MainNavigationController.navigate(R.id.newsFragment, any()) }
                coVerify(exactly = 1) { userRepository.readNotification("1") }
            }

            "open the party chat for party messages and the guild otherwise" {
                val viewModel = makeViewModel()
                userData.value = User().apply { party = UserParty().apply { id = "party-1" } }
                serverNotifications.value = listOf(partyMessage("1", "party-1"), partyMessage("2", "guild-1"))
                viewModel.click("1", MainNavigationController)
                verify { MainNavigationController.navigate(R.id.partyFragment, any()) }
                viewModel.click("2", MainNavigationController)
                verify { MainNavigationController.navigate(R.id.guildFragment, any()) }
            }

            "open the stats for unallocated points" {
                serverNotifications.value = listOf(notification("1", Notification.Type.UNALLOCATED_STATS_POINTS))
                makeViewModel().click("1", MainNavigationController)
                verify { MainNavigationController.navigate(R.id.statsFragment, any()) }
            }

            "do nothing for unknown notifications" {
                makeViewModel().click("missing", MainNavigationController)
                verify(exactly = 0) { MainNavigationController.navigate(any<Int>(), any()) }
            }
        }

        "accept" should {
            "join the group for a party invitation and refresh the user" {
                val viewModel = makeViewModel()
                userData.value = User().apply { invitations = Invitations().apply { parties = RealmList(PartyInvite().apply { id = "party-1" }) } }
                viewModel.accept("custom-party-invitation-party-1")
                coVerify(exactly = 1) { socialRepository.joinGroup("party-1") }
                coVerify { userRepository.retrieveUser(false, true) }
            }

            "accept the quest for a quest invitation" {
                val viewModel = makeViewModel()
                val user = User().apply { party = UserParty().apply { id = "party-1"; quest = Quest().apply { rsvpNeeded = true } } }
                userData.value = user
                viewModel.accept("custom-quest-invitation-party-1")
                coVerify(exactly = 1) { socialRepository.acceptQuest(any(), "party-1") }
            }
        }

        "reject" should {
            "reject a guild invitation" {
                val viewModel = makeViewModel()
                userData.value = User().apply { invitations = Invitations().apply { guilds = RealmList(GuildInvite().apply { id = "guild-1" }) } }
                viewModel.reject("custom-guild-invitation-guild-1")
                coVerify(exactly = 1) { socialRepository.rejectGroupInvite("guild-1") }
            }

            "reject the quest for a quest invitation" {
                val viewModel = makeViewModel()
                userData.value = User().apply { party = UserParty().apply { id = "party-1"; quest = Quest().apply { rsvpNeeded = true } } }
                viewModel.reject("custom-quest-invitation-party-1")
                coVerify(exactly = 1) { socialRepository.rejectQuest(any(), "party-1") }
            }
        }
    })
