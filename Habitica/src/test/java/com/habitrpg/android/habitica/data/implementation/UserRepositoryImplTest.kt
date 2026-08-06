package com.habitrpg.android.habitica.data.implementation

import android.content.Context
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.data.local.InventoryLocalRepository
import com.habitrpg.android.habitica.data.local.UserLocalRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.models.TeamPlan
import com.habitrpg.android.habitica.models.TutorialStep
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.responses.UnlockResponse
import com.habitrpg.android.habitica.models.social.UserParty
import com.habitrpg.android.habitica.models.user.Gear
import com.habitrpg.android.habitica.models.user.Items
import com.habitrpg.android.habitica.models.user.Preferences
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import com.habitrpg.shared.habitica.models.tasks.Attribute
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf

class UserRepositoryImplTest :
    WordSpec({
        lateinit var repository: UserRepository
        val localRepository = mockk<UserLocalRepository>()
        val apiClient = mockk<ApiClient>()
        val authenticationHandler = mockk<AuthenticationHandler>()
        val taskRepository = mockk<TaskRepository>()
        val appConfigManager = mockk<AppConfigManager>()
        val context = mockk<Context>(relaxed = true)
        val inventoryLocalRepository = mockk<InventoryLocalRepository>()
        beforeEach {
            every { authenticationHandler.currentUserID } returns "user-1"
            repository =
                UserRepositoryImpl(
                    localRepository,
                    apiClient,
                    authenticationHandler,
                    taskRepository,
                    appConfigManager,
                    context,
                    inventoryLocalRepository,
                )
        }
        afterEach { clearAllMocks() }
        "updateUser(key, value)" should {
            "return the old user unchanged when the API returns nothing" {
                coEvery { apiClient.updateUser(mapOf("preferences.language" to "de")) } returns null
                val result = repository.updateUser("preferences.language", "de")
                result shouldBe null
            }

            "merge the network response onto the existing local user" {
                val oldUser = User().apply { id = "user-1" }
                val networkUser =
                    User().apply {
                        id = "user-1"
                        items = Items()
                    }
                coEvery { apiClient.updateUser(mapOf("preferences.language" to "de")) } returns networkUser
                every { localRepository.getUser("user-1") } returns flowOf(oldUser)
                every { localRepository.saveUser(any(), false) } returns Unit
                val result = repository.updateUser("preferences.language", "de")
                result shouldBe oldUser
                result?.items shouldBe networkUser.items
                verify { localRepository.saveUser(oldUser, false) }
            }

            "fill in full equipment details for owned-gear entries that only arrived as a boolean flag" {
                val oldUser = User().apply { id = "user-1" }
                val thinOwnedItem = Equipment().apply { key = "sword_1"; owned = true }
                val networkUser =
                    User().apply {
                        id = "user-1"
                        items = Items().apply { gear = Gear().apply { owned = io.realm.RealmList(thinOwnedItem) } }
                    }
                val knownItem = Equipment().apply { key = "sword_1"; text = "Sword"; value = 5.0 }
                coEvery { apiClient.updateUser(mapOf("preferences.language" to "de")) } returns networkUser
                every { localRepository.getUser("user-1") } returns flowOf(oldUser)
                every { localRepository.saveUser(any(), false) } returns Unit
                coEvery { inventoryLocalRepository.getEquipment(listOf("sword_1")) } returns flowOf(listOf(knownItem))
                repository.updateUser("preferences.language", "de")
                thinOwnedItem.text shouldBe "Sword"
                thinOwnedItem.value shouldBe 5.0
                thinOwnedItem.owned shouldBe true
            }

            "inherit the previous quest RSVP state when the response omits RSVPNeeded" {
                val oldUser =
                    User().apply {
                        id = "user-1"
                        party = UserParty().apply { quest = Quest().apply { rsvpNeeded = true } }
                    }
                val networkUser =
                    User().apply {
                        id = "user-1"
                        party = UserParty().apply { quest = Quest().apply { rsvpNeededWasSpecified = false } }
                    }
                coEvery { apiClient.updateUser(mapOf("preferences.language" to "de")) } returns networkUser
                every { localRepository.getUser("user-1") } returns flowOf(oldUser)
                every { localRepository.saveUser(any(), false) } returns Unit
                val result = repository.updateUser("preferences.language", "de")
                result?.party?.quest?.rsvpNeeded shouldBe true
            }

            "use the response's own RSVPNeeded value when it was explicitly specified" {
                val oldUser =
                    User().apply {
                        id = "user-1"
                        party = UserParty().apply { quest = Quest().apply { rsvpNeeded = true } }
                    }
                val networkUser =
                    User().apply {
                        id = "user-1"
                        party = UserParty().apply { quest = Quest().apply { rsvpNeeded = false; rsvpNeededWasSpecified = true } }
                    }
                coEvery { apiClient.updateUser(mapOf("preferences.language" to "de")) } returns networkUser
                every { localRepository.getUser("user-1") } returns flowOf(oldUser)
                every { localRepository.saveUser(any(), false) } returns Unit
                val result = repository.updateUser("preferences.language", "de")
                result?.party?.quest?.rsvpNeeded shouldBe false
            }
        }
        "resetTutorial" should {
            "return null when there are no tutorial steps" {
                coEvery { localRepository.getTutorialSteps() } returns flowOf()
                val result = repository.resetTutorial()
                result shouldBe null
            }

            "clear every step's flag via updateUser" {
                val step = TutorialStep().apply { tutorialGroup = "tasks"; identifier = "intro" }
                coEvery { localRepository.getTutorialSteps() } returns flowOf(listOf(step))
                coEvery { apiClient.updateUser(mapOf(step.flagPath to false)) } returns null
                repository.resetTutorial()
                coVerify { apiClient.updateUser(mapOf(step.flagPath to false)) }
            }
        }
        "sleep" should {
            "toggle the preference locally and keep it when the API succeeds" {
                val user = User().apply { preferences = Preferences().apply { sleep = false } }
                every { localRepository.modify(user, any()) } answers { secondArg<(User) -> Unit>().invoke(user) }
                coEvery { apiClient.sleep() } returns true
                repository.sleep(user)
                user.preferences?.sleep shouldBe true
            }

            "revert the local change when the API fails" {
                val user = User().apply { preferences = Preferences().apply { sleep = false } }
                every { localRepository.modify(user, any()) } answers { secondArg<(User) -> Unit>().invoke(user) }
                coEvery { apiClient.sleep() } returns null
                repository.sleep(user)
                user.preferences?.sleep shouldBe false
            }
        }
        "unlockPath" should {
            "return null and change nothing local when the API returns nothing" {
                coEvery { apiClient.unlockPath("hair.color.red") } returns null
                val result = repository.unlockPath("hair.color.red", 20)
                result shouldBe null
                verify(exactly = 0) { localRepository.modify(any<User>(), any()) }
            }

            "apply the unlock response to the live user and deduct the balance" {
                val user = User().apply { id = "user-1"; balance = 4.0 }
                val response = UnlockResponse().apply { items = Items() }
                coEvery { apiClient.unlockPath("hair.color.red") } returns response
                every { localRepository.getUser("user-1") } returns flowOf(user)
                every { localRepository.modify(user, any()) } answers { secondArg<(User) -> Unit>().invoke(user) }
                val result = repository.unlockPath("hair.color.red", 20)
                result shouldBe response
                user.items shouldBe response.items
                user.balance shouldBe 4.0 - (20 / 4.0)
            }
        }
        "readNotification" should {
            "read a new notification id" {
                coEvery { apiClient.readNotification("note-1") } returns null
                val result = repository.readNotification("note-1")
                result shouldBe null
                coVerify { apiClient.readNotification("note-1") }
            }

            "skip re-reading the same notification id twice in a row" {
                coEvery { apiClient.readNotification("note-1") } returns null
                repository.readNotification("note-1")
                repository.readNotification("note-1")
                coVerify(exactly = 1) { apiClient.readNotification("note-1") }
            }
        }
        "changeCustomDayStart" should {
            "update remotely and locally" {
                coEvery { apiClient.changeCustomDayStart(mapOf("dayStart" to 3)) } returns null
                val user = User()
                every { localRepository.updateDayStartTime("user-1", 3) } returns user
                val result = repository.changeCustomDayStart(3)
                result shouldBe user
                coVerify { apiClient.changeCustomDayStart(mapOf("dayStart" to 3)) }
            }
        }
        "retrieveAchievements" should {
            "return null and save nothing when the API returns nothing" {
                coEvery { apiClient.getMemberAchievements("user-1") } returns null
                val result = repository.retrieveAchievements()
                result shouldBe null
                verify(exactly = 0) { localRepository.save(any<List<Achievement>>()) }
            }

            "save and return the achievements from the API" {
                val achievements = listOf(Achievement())
                coEvery { apiClient.getMemberAchievements("user-1") } returns achievements
                every { localRepository.save(achievements) } returns Unit
                val result = repository.retrieveAchievements()
                result shouldBe achievements
                verify { localRepository.save(achievements) }
            }
        }
        "retrieveTeamPlans" should {
            "tag every team with the current user id and save them" {
                val team1 = TeamPlan().apply { id = "team-1" }
                val team2 = TeamPlan().apply { id = "team-2" }
                coEvery { apiClient.getTeamPlans() } returns listOf(team1, team2)
                every { localRepository.save(listOf(team1, team2)) } returns Unit
                val result = repository.retrieveTeamPlans()
                result shouldBe listOf(team1, team2)
                team1.userID shouldBe "user-1"
                team2.userID shouldBe "user-1"
            }

            "return null and save nothing when the API returns nothing" {
                coEvery { apiClient.getTeamPlans() } returns null
                val result = repository.retrieveTeamPlans()
                result shouldBe null
                verify(exactly = 0) { localRepository.save(any<List<TeamPlan>>()) }
            }
        }
        "allocatePoint" should {
            "return null and update nothing when there is no live user" {
                every { localRepository.getUser("user-1") } returns flowOf(null)
                coEvery { apiClient.allocatePoint(Attribute.STRENGTH.value) } returns null
                val result = repository.allocatePoint(Attribute.STRENGTH)
                result shouldBe null
                verify(exactly = 0) { localRepository.updateStats(any(), any()) }
            }

            "increment the chosen stat locally and persist the API result" {
                val user = User().apply { stats = Stats().apply { strength = 1; points = 2 } }
                every { localRepository.getUser("user-1") } returns flowOf(user)
                every { localRepository.getLiveObject(user) } returns user
                every { localRepository.updateStats("user-1", any()) } returns Unit
                val apiStats = Stats().apply { strength = 2; points = 1 }
                coEvery { apiClient.allocatePoint(Attribute.STRENGTH.value) } returns apiStats
                val result = repository.allocatePoint(Attribute.STRENGTH)
                result shouldBe apiStats
                verify(exactly = 2) { localRepository.updateStats("user-1", any()) }
            }
        }
    })
