package com.habitrpg.android.habitica.ui.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.api.MaintenanceApiService
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.TaskAlarmManager
import com.habitrpg.android.habitica.helpers.notifications.PushNotificationManager
import com.habitrpg.android.habitica.models.TutorialStep
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.social.UserParty
import com.habitrpg.android.habitica.models.user.Profile
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.common.habitica.api.HostConfig
import com.habitrpg.shared.habitica.models.responses.MaintenanceResponse
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class MainActivityViewModelTest :
    WordSpec({
        val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
        val userRepository = mockk<UserRepository>(relaxed = true)
        val mainUserViewModel = mockk<MainUserViewModel>()
        val hostConfig = mockk<HostConfig>(relaxed = true)
        val pushNotificationManager = mockk<PushNotificationManager>(relaxed = true)
        val sharedPreferences = mockk<SharedPreferences>(relaxed = true)
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        val contentRepository = mockk<ContentRepository>(relaxed = true)
        val taskRepository = mockk<TaskRepository>(relaxed = true)
        val inventoryRepository = mockk<InventoryRepository>(relaxed = true)
        val taskAlarmManager = mockk<TaskAlarmManager>(relaxed = true)
        val maintenanceService = mockk<MaintenanceApiService>()
        val userData = MutableLiveData<User?>()

        every { mainUserViewModel.user } returns userData
        every { sharedPreferences.edit() } returns editor

        fun makeViewModel() =
            MainActivityViewModel(
                userRepository,
                mainUserViewModel,
                hostConfig,
                pushNotificationManager,
                sharedPreferences,
                contentRepository,
                taskRepository,
                inventoryRepository,
                taskAlarmManager,
                maintenanceService,
                mockk(relaxed = true),
            )

        beforeEach {
            Dispatchers.setMain(testDispatcher)
            every { hostConfig.isInitialized } returns true
            every { hostConfig.hasAuthentication() } returns true
            userData.value = null
        }
        afterEach {
            clearMocks(userRepository, pushNotificationManager, editor, contentRepository, taskRepository, inventoryRepository, taskAlarmManager, answers = false)
        }

        "isAuthenticated" should {
            "return whether the host config has authentication" {
                makeViewModel().isAuthenticated shouldBe true
                every { hostConfig.hasAuthentication() } returns false
                makeViewModel().isAuthenticated shouldBe false
            }
        }

        "preferenceLanguage" should {
            "read and write the language preference" {
                every { sharedPreferences.getString("language", "en") } returns "de"
                val viewModel = makeViewModel()
                viewModel.preferenceLanguage shouldBe "de"
                viewModel.preferenceLanguage = "fr"
                verify { editor.putString("language", "fr") }
            }
        }

        "onCreate" should {
            "schedule saved alarms" {
                every { sharedPreferences.getBoolean("preventDailyReminder", false) } returns true
                makeViewModel().onCreate()
                coVerify(exactly = 1) { taskAlarmManager.scheduleAllSavedAlarms(true) }
            }
        }

        "onResume" should {
            "store the launch time and allow the daily reminder" {
                makeViewModel().onResume()
                verify { editor.putLong("lastAppLaunch", any()) }
                verify { editor.putBoolean("preventDailyReminder", false) }
            }
        }

        "retrieveUser" should {
            "retrieve user, content and team plans" {
                val user = User()
                coEvery { userRepository.retrieveUser(true, true) } returns user
                every { pushNotificationManager.notificationPermissionEnabled() } returns true
                makeViewModel().retrieveUser(true)
                coVerify(exactly = 1) { contentRepository.retrieveWorldState() }
                verify(exactly = 1) { pushNotificationManager.setUser(user) }
                verify(exactly = 1) { pushNotificationManager.addPushDeviceUsingStoredToken() }
                coVerify(exactly = 1) { inventoryRepository.retrieveInAppRewards() }
                coVerify(exactly = 1) { contentRepository.retrieveContent() }
                coVerify(exactly = 1) { userRepository.retrieveTeamPlans() }
            }

            "request notification permission if push notifications are wanted" {
                coEvery { userRepository.retrieveUser(true, false) } returns User()
                every { pushNotificationManager.notificationPermissionEnabled() } returns false
                every { sharedPreferences.getBoolean("usePushNotifications", true) } returns true
                val viewModel = makeViewModel()
                viewModel.retrieveUser()
                viewModel.requestNotificationPermission.value shouldBe true
            }

            "do nothing without authentication" {
                every { hostConfig.hasAuthentication() } returns false
                makeViewModel().retrieveUser()
                coVerify(exactly = 0) { userRepository.retrieveUser(any(), any()) }
                coVerify(exactly = 0) { userRepository.retrieveTeamPlans() }
            }

            "wait for the host config to be ready" {
                every { hostConfig.isInitialized } returns false
                makeViewModel().retrieveUser()
                coVerify(exactly = 2) { hostConfig.awaitReady() }
            }
        }

        "updateAllowPushNotifications" should {
            "store the preference" {
                makeViewModel().updateAllowPushNotifications(false)
                verify { editor.putBoolean("usePushNotifications", false) }
            }
        }

        "onTutorialCompleted" should {
            "mark the tutorial step as completed" {
                makeViewModel().onTutorialCompleted(TutorialStep().apply { tutorialGroup = "common"; identifier = "habits" })
                coVerify(exactly = 1) { userRepository.updateUser("flags.tutorial.common.habits", true) }
            }
        }

        "ifNeedsMaintenance" should {
            "report active maintenance" {
                val response = MaintenanceResponse().apply { activeMaintenance = true }
                coEvery { maintenanceService.getMaintenanceStatus() } returns response
                var result: MaintenanceResponse? = null
                makeViewModel().ifNeedsMaintenance { result = it }
                result shouldBe response
            }

            "not report anything without maintenance" {
                coEvery { maintenanceService.getMaintenanceStatus() } returns MaintenanceResponse()
                var called = false
                makeViewModel().ifNeedsMaintenance { called = true }
                called shouldBe false
            }
        }

        "getToolbarTitle" should {
            "use the egg text for pet and mount details" {
                every { inventoryRepository.getItem("egg", "Wolf") } returns flowOf(Egg().apply { text = "Wolf"; mountText = "Wolf Steed" })
                val viewModel = makeViewModel()
                var title: CharSequence? = null
                viewModel.getToolbarTitle(R.id.petDetailRecyclerFragment, null, "Wolf") { title = it }
                title shouldBe "Wolf"
                viewModel.getToolbarTitle(R.id.mountDetailRecyclerFragment, null, "Wolf") { title = it }
                title shouldBe "Wolf Steed"
            }

            "use the user name if there is no label" {
                userData.value = User().apply { profile = Profile().apply { name = "Tester" } }
                var title: CharSequence? = null
                makeViewModel().getToolbarTitle(0, null, null) { title = it }
                title shouldBe "Tester"
            }

            "use the label if there is one" {
                var title: CharSequence? = null
                makeViewModel().getToolbarTitle(0, "Tasks", null) { title = it }
                title shouldBe "Tasks"
            }

            "use an empty title for the promo info" {
                var title: CharSequence? = null
                makeViewModel().getToolbarTitle(R.id.promoInfoFragment, "Promo", null) { title = it }
                title shouldBe ""
            }
        }
    })
