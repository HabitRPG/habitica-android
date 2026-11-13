package com.habitrpg.android.habitica.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.NotificationsManager
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.inventory.QuestMember
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.social.UserParty
import com.habitrpg.android.habitica.models.user.User
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.realm.RealmList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class PartyViewModelTest :
    WordSpec({
        val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
        val userRepository = mockk<UserRepository>(relaxed = true)
        val mainUserViewModel = mockk<MainUserViewModel>()
        val challengeRepository = mockk<ChallengeRepository>(relaxed = true)
        val socialRepository = mockk<SocialRepository>(relaxed = true)
        val notificationsManager = mockk<NotificationsManager>()
        val userData = MutableLiveData<User?>()

        every { mainUserViewModel.user } returns userData
        every { notificationsManager.getNotifications() } returns flowOf(emptyList())

        fun makeViewModel(party: Group? = null) =
            PartyViewModel(userRepository, mainUserViewModel, challengeRepository, socialRepository, notificationsManager).apply {
                if (party != null) {
                    every { socialRepository.getGroup(party.id) } returns flowOf(party)
                    setGroupID(party.id)
                    getGroupData().getOrAwaitValue()
                }
            }

        fun party(quest: Quest) =
            Group().apply {
                id = "party-1"
                this.quest = quest
            }

        beforeEach {
            Dispatchers.setMain(testDispatcher)
            userData.value = User().apply { id = "user-1" }
        }
        afterEach { clearMocks(userRepository, socialRepository, answers = false) }

        "isQuestActive" should {
            "return true if the party quest is active" {
                makeViewModel(party(Quest().apply { active = true })).isQuestActive shouldBe true
            }

            "return false without a party" {
                makeViewModel().isQuestActive shouldBe false
            }
        }

        "isUserOnQuest" should {
            "return true if the user is a quest member" {
                val quest = Quest().apply { members = RealmList(QuestMember().apply { key = "user-1" }) }
                makeViewModel(party(quest)).isUserOnQuest shouldBe true
            }

            "return false if the user is not a quest member" {
                val quest = Quest().apply { members = RealmList(QuestMember().apply { key = "user-2" }) }
                makeViewModel(party(quest)).isUserOnQuest shouldBe false
            }
        }

        "isUserQuestLeader" should {
            "return true if the user leads the quest" {
                makeViewModel(party(Quest().apply { leader = "user-1" })).isUserQuestLeader shouldBe true
            }

            "return false if someone else leads the quest" {
                makeViewModel(party(Quest().apply { leader = "user-2" })).isUserQuestLeader shouldBe false
            }
        }

        "showParticipantButtons" should {
            "return true if the user needs to respond to an inactive quest" {
                userData.value = User().apply { party = UserParty().apply { quest = Quest().apply { rsvpNeeded = true } } }
                makeViewModel().showParticipantButtons() shouldBe true
            }

            "return false if the quest is already active" {
                userData.value = User().apply { party = UserParty().apply { quest = Quest().apply { rsvpNeeded = true } } }
                makeViewModel(party(Quest().apply { active = true })).showParticipantButtons() shouldBe false
            }

            "return false if no response is needed" {
                userData.value = User().apply { party = UserParty().apply { quest = Quest() } }
                makeViewModel().showParticipantButtons() shouldBe false
            }
        }

        "acceptQuest" should {
            "accept the quest and refresh party and user" {
                makeViewModel(party(Quest())).acceptQuest()
                coVerify(exactly = 1) { socialRepository.acceptQuest(userData.value, "party-1") }
                coVerify(exactly = 1) { socialRepository.retrieveGroup("party-1") }
                coVerify(exactly = 1) { userRepository.retrieveUser() }
            }

            "do nothing without a party" {
                makeViewModel().acceptQuest()
                coVerify(exactly = 0) { socialRepository.acceptQuest(any(), any()) }
            }
        }

        "rejectQuest" should {
            "reject the quest and refresh party and user" {
                makeViewModel(party(Quest())).rejectQuest()
                coVerify(exactly = 1) { socialRepository.rejectQuest(userData.value, "party-1") }
                coVerify(exactly = 1) { socialRepository.retrieveGroup("party-1") }
                coVerify(exactly = 1) { userRepository.retrieveUser() }
            }
        }

        "loadPartyID" should {
            "use the party of the user as group" {
                every { userRepository.getUser() } returns flowOf(User().apply { party = UserParty().apply { id = "party-2" } })
                val viewModel = makeViewModel()
                viewModel.loadPartyID()
                viewModel.groupID shouldBe "party-2"
            }
        }

        "getMembersData" should {
            "load the members of the party" {
                val members = listOf(Member().apply { id = "user-1" })
                every { socialRepository.getPartyMembers("party-1") } returns flowOf(members)
                makeViewModel(party(Quest())).getMembersData().getOrAwaitValue() shouldBe members
            }
        }
    })
