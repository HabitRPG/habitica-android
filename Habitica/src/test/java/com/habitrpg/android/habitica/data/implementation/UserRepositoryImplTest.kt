package com.habitrpg.android.habitica.data.implementation

import android.content.Context
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.data.local.InventoryLocalRepository
import com.habitrpg.android.habitica.data.local.UserLocalRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.models.TeamPlan
import com.habitrpg.android.habitica.models.TutorialStep
import com.habitrpg.android.habitica.models.auth.LocalAuthentication
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.responses.SkillResponse
import com.habitrpg.android.habitica.models.responses.UnlockResponse
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.social.GroupMembership
import com.habitrpg.android.habitica.models.social.UserParty
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.models.user.Authentication
import com.habitrpg.android.habitica.models.user.Flags
import com.habitrpg.android.habitica.models.user.Gear
import com.habitrpg.android.habitica.models.user.Hair
import com.habitrpg.android.habitica.models.user.Items
import com.habitrpg.android.habitica.models.user.OwnedEquipment
import com.habitrpg.android.habitica.models.user.Preferences
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import com.habitrpg.android.habitica.widget.glance.work.WidgetRefreshWorker
import com.habitrpg.common.habitica.models.notifications.NewStuffData
import com.habitrpg.shared.habitica.models.tasks.Attribute
import com.habitrpg.shared.habitica.models.tasks.TasksOrder
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import io.realm.RealmList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.TimeUnit

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
                        party =
                            UserParty().apply {
                                quest =
                                    Quest().apply {
                                        rsvpNeeded = false
                                        rsvpNeededWasSpecified = true
                                    }
                            }
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
                val step =
                    TutorialStep().apply {
                        tutorialGroup = "tasks"
                        identifier = "intro"
                    }
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
                val user =
                    User().apply {
                        id = "user-1"
                        balance = 4.0
                    }
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
                val user =
                    User().apply {
                        stats =
                            Stats().apply {
                                strength = 1
                                points = 2
                            }
                    }
                every { localRepository.getUser("user-1") } returns flowOf(user)
                every { localRepository.getLiveObject(user) } returns user
                every { localRepository.updateStats("user-1", any()) } returns Unit
                val apiStats =
                    Stats().apply {
                        strength = 2
                        points = 1
                    }
                coEvery { apiClient.allocatePoint(Attribute.STRENGTH.value) } returns apiStats
                val result = repository.allocatePoint(Attribute.STRENGTH)
                result shouldBe apiStats
                verify(exactly = 2) { localRepository.updateStats("user-1", any()) }
            }
        }
        "retrieveUser" should {
            val offset = -TimeUnit.MINUTES.convert(TimeZone.getDefault().getOffset(Date().time).toLong(), TimeUnit.MILLISECONDS).toInt()

            fun networkUser() =
                User().apply {
                    id = "user-1"
                    preferences = Preferences().apply { timezoneOffset = offset }
                }

            beforeEach {
                Dispatchers.setMain(UnconfinedTestDispatcher())
                every { localRepository.saveUser(any()) } returns Unit
            }

            "save the user and its tasks" {
                val tasks = TaskList()
                val order = TasksOrder()
                val user = networkUser().apply { this.tasks = tasks; tasksOrder = order }
                coEvery { apiClient.retrieveUser(true) } returns user
                coEvery { taskRepository.saveTasks("user-1", order, tasks) } returns Unit
                repository.retrieveUser(withTasks = true, forced = true) shouldBe user
                verify { localRepository.saveUser(user) }
                coVerify { taskRepository.saveTasks("user-1", order, tasks) }
            }

            "only retrieve the user again after a few minutes unless forced" {
                coEvery { apiClient.retrieveUser(false) } returns networkUser()
                repository.retrieveUser()
                repository.retrieveUser() shouldBe null
                repository.retrieveUser(forced = true)
                coVerify(exactly = 2) { apiClient.retrieveUser(false) }
            }

            "keep the local quest RSVP state if the response does not specify it" {
                val user = networkUser().apply { party = UserParty().apply { quest = Quest().apply { rsvpNeededWasSpecified = false } } }
                val oldUser = User().apply { party = UserParty().apply { quest = Quest().apply { rsvpNeeded = true } } }
                coEvery { apiClient.retrieveUser(false) } returns user
                every { localRepository.getUser("user-1") } returns flowOf(oldUser)
                repository.retrieveUser(forced = true)
                user.party?.quest?.rsvpNeeded shouldBe true
            }

            "update the timezone offset if it changed" {
                val user = networkUser().apply { preferences?.timezoneOffset = offset + 60 }
                coEvery { apiClient.retrieveUser(false) } returns user
                coEvery { apiClient.updateUser(mapOf("preferences.timezoneOffset" to offset.toString())) } returns null
                repository.retrieveUser(forced = true)
                coVerify { apiClient.updateUser(mapOf("preferences.timezoneOffset" to offset.toString())) }
            }

            "return null if the request failed" {
                coEvery { apiClient.retrieveUser(false) } returns null
                repository.retrieveUser(forced = true) shouldBe null
                verify(exactly = 0) { localRepository.saveUser(any()) }
            }
        }
        "syncUserStats" should {
            "save the synced user if its stats are complete" {
                val user = User().apply { stats = Stats().apply { toNextLevel = 100; maxMP = 30 } }
                coEvery { apiClient.syncUserStats() } returns user
                every { localRepository.saveUser(user) } returns Unit
                repository.syncUserStats() shouldBe user
                verify { localRepository.saveUser(user) }
            }

            "retrieve the full user if the stats are incomplete" {
                Dispatchers.setMain(UnconfinedTestDispatcher())
                coEvery { apiClient.syncUserStats() } returns User()
                coEvery { apiClient.retrieveUser(false) } returns null
                repository.syncUserStats()
                coVerify { apiClient.retrieveUser(false) }
            }
        }
        "revive" should {
            "return the equipment that was lost" {
                Dispatchers.setMain(UnconfinedTestDispatcher())
                val user = User().apply { items = Items().apply { gear = Gear().apply { owned = RealmList(OwnedEquipment().apply { key = "weapon_1"; owned = true }) } } }
                val revivedItems = Items().apply { gear = Gear().apply { owned = RealmList(OwnedEquipment().apply { key = "weapon_1"; owned = false }) } }
                val lostEquipment = Equipment().apply { key = "weapon_1" }
                coEvery { apiClient.revive() } returns revivedItems
                every { localRepository.getLiveUser("user-1") } returns user
                coEvery { apiClient.retrieveUser(false) } returns null
                every { inventoryLocalRepository.getEquipment("weapon_1") } returns flowOf(lostEquipment)
                repository.revive() shouldBe lostEquipment
            }
        }
        "useSkill" should {
            "calculate the differences to the current user" {
                val user = User().apply { stats = Stats().apply { hp = 40.0; exp = 10.0; gp = 5.0 } }
                val response =
                    SkillResponse().apply {
                        this.user = User().apply { stats = Stats().apply { hp = 50.0; exp = 25.0; gp = 3.0 } }
                    }
                coEvery { apiClient.useSkill("heal", "self", "task-1") } returns response
                every { localRepository.getUser("user-1") } returns flowOf(user)
                every { localRepository.getLiveObject(user) } returns user
                every { localRepository.saveUser(any(), false) } returns Unit
                repository.useSkill("heal", "self", "task-1") shouldBe response
                response.hpDiff shouldBe 10.0
                response.expDiff shouldBe 15.0
                response.goldDiff shouldBe -2.0
            }

            "return the response without a local user" {
                val response = SkillResponse()
                coEvery { apiClient.useSkill("fireball", "party") } returns response
                every { localRepository.getUser("user-1") } returns flowOf(null)
                repository.useSkill("fireball", "party") shouldBe response
            }
        }
        "useCustomization" should {
            val user = User().apply { preferences = Preferences().apply { hair = Hair() } }

            beforeEach {
                every { appConfigManager.enableLocalChanges() } returns true
                every { localRepository.getUser("user-1") } returns flowOf(user)
                every { localRepository.getLiveObject(user) } returns user
                every { localRepository.modify(any<User>(), any()) } answers { secondArg<(User) -> Unit>().invoke(firstArg()) }
                coEvery { apiClient.updateUser(any()) } returns null
            }

            "apply the customization locally and update the category path" {
                repository.useCustomization("hair", "bangs", "2")
                user.preferences?.hair?.bangs shouldBe 2
                coVerify { apiClient.updateUser(mapOf("preferences.hair.bangs" to "2")) }
            }

            "update the type path without category" {
                repository.useCustomization("skin", null, "ddc994")
                user.preferences?.skin shouldBe "ddc994"
                coVerify { apiClient.updateUser(mapOf("preferences.skin" to "ddc994")) }
            }

            "unlock backgrounds before using them" {
                Dispatchers.setMain(UnconfinedTestDispatcher())
                coEvery { apiClient.unlockPath("background.beach") } returns null
                coEvery { apiClient.retrieveUser(false) } returns null
                repository.useCustomization("background", null, "beach")
                user.preferences?.background shouldBe "beach"
                coVerify { apiClient.unlockPath("background.beach") }
            }

            "clear the background without unlocking" {
                repository.useCustomization("background", null, "")
                coVerify { apiClient.updateUser(mapOf("preferences.background" to "")) }
                coVerify(exactly = 0) { apiClient.unlockPath(any()) }
            }
        }
        "bulkAllocatePoints" should {
            "copy the allocated stats to the user" {
                val user = User().apply { stats = Stats() }
                val stats = Stats().apply { strength = 1; intelligence = 2; constitution = 3; per = 4; points = 0 }
                coEvery { apiClient.bulkAllocatePoints(1, 2, 3, 4) } returns stats
                every { localRepository.getUser("user-1") } returns flowOf(user)
                every { localRepository.getLiveObject(user) } returns user
                every { localRepository.modify(any<User>(), any()) } answers { secondArg<(User) -> Unit>().invoke(firstArg()) }
                repository.bulkAllocatePoints(1, 2, 3, 4) shouldBe stats
                user.stats?.intelligence shouldBe 2
                user.stats?.per shouldBe 4
            }
        }
        "runCron" should {
            "score the given tasks, run cron and refresh the user" {
                Dispatchers.setMain(UnconfinedTestDispatcher())
                mockkObject(WidgetRefreshWorker)
                coEvery { WidgetRefreshWorker.refreshAllWidgetsNow(any()) } returns Unit
                val user = User().apply { needsCron = true }
                every { localRepository.getUser("user-1") } returns flowOf(user)
                every { localRepository.getLiveObject(user) } returns user
                every { localRepository.modify(any<User>(), any()) } answers { secondArg<(User) -> Unit>().invoke(firstArg()) }
                coEvery { taskRepository.bulkScoreTasks(any()) } returns null
                coEvery { apiClient.runCron() } returns null
                coEvery { apiClient.retrieveUser(true) } returns null
                repository.runCron(mutableListOf(Task().apply { id = "task-1" }))
                user.needsCron shouldBe false
                coVerify { taskRepository.bulkScoreTasks(listOf(mapOf("id" to "task-1", "direction" to "up"))) }
                coVerify { apiClient.runCron() }
                coVerify { WidgetRefreshWorker.refreshAllWidgetsNow(context) }
                unmockkObject(WidgetRefreshWorker)
            }
        }
        "updateLoginName" should {
            "use the password endpoint if a password is given" {
                val user = User().apply { authentication = Authentication().apply { localAuthentication = LocalAuthentication() }; flags = Flags() }
                coEvery { apiClient.updateLoginName("tester", "secret") } returns null
                every { localRepository.getUser("user-1") } returns flowOf(user)
                every { localRepository.modify(any<User>(), any()) } answers { secondArg<(User) -> Unit>().invoke(firstArg()) }
                repository.updateLoginName(" tester ", "secret ") shouldBe user
                user.authentication?.localAuthentication?.username shouldBe " tester "
                user.flags?.verifiedUsername shouldBe true
            }

            "update only the username without a password" {
                coEvery { apiClient.updateUsername("tester") } returns null
                every { localRepository.getUser("user-1") } returns flowOf(null)
                repository.updateLoginName("tester", null) shouldBe null
                coVerify { apiClient.updateUsername("tester") }
            }
        }
        "resetAccount" should {
            "retrieve the user after a successful reset" {
                Dispatchers.setMain(UnconfinedTestDispatcher())
                coEvery { apiClient.resetAccount("secret") } returns true
                coEvery { apiClient.retrieveUser(true) } returns null
                repository.resetAccount("secret") shouldBe true
                coVerify { apiClient.retrieveUser(true) }
            }

            "not retrieve the user if the reset failed" {
                coEvery { apiClient.resetAccount("wrong") } returns false
                repository.resetAccount("wrong") shouldBe false
                coVerify(exactly = 0) { apiClient.retrieveUser(any()) }
            }
        }
        "retrieveTeamPlan" should {
            "save the team, its tasks and its members" {
                val order = TasksOrder()
                val team = Group().apply { id = "team-1"; tasksOrder = order }
                val tasks = TaskList()
                val member = Member().apply { id = "member-1" }
                coEvery { apiClient.getGroup("team-1") } returns team
                coEvery { apiClient.getTeamPlanTasks("team-1") } returns tasks
                coEvery { apiClient.getGroupMembers("team-1", true) } returns listOf(member)
                coEvery { taskRepository.saveTasks("team-1", order, tasks) } returns Unit
                every { localRepository.save(any<Group>()) } returns Unit
                every { localRepository.save(any<List<BaseObject>>()) } returns Unit
                repository.retrieveTeamPlan("team-1") shouldBe team
                coVerify { taskRepository.saveTasks("team-1", order, tasks) }
                verify { localRepository.save(match<List<BaseObject>> { (it.singleOrNull() as? GroupMembership)?.combinedID == "member-1team-1" }) }
                verify { localRepository.save(listOf(member)) }
            }

            "return null if the team could not be loaded" {
                coEvery { apiClient.getGroup("team-1") } returns null
                repository.retrieveTeamPlan("team-1") shouldBe null
            }
        }
        "getNewsNotification" should {
            "use the title of the latest news" {
                coEvery { apiClient.getNews() } returns listOf(mapOf("title" to "New Quest"))
                val notification = repository.getNewsNotification()
                notification?.id shouldBe "custom-new-stuff-notification"
                (notification?.data as NewStuffData).title shouldBe "New Quest"
            }
        }
    })
