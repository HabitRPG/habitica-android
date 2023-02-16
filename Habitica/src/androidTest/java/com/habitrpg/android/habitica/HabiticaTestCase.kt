package com.habitrpg.android.habitica

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.habitrpg.android.habitica.api.GSonFactoryCreator
import com.habitrpg.android.habitica.api.MaintenanceApiService
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.TagRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.TutorialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.NotificationsManager
import com.habitrpg.android.habitica.helpers.SoundManager
import com.habitrpg.android.habitica.interactors.FeedPetUseCase
import com.habitrpg.android.habitica.interactors.HatchPetUseCase
import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.Food
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.common.habitica.helpers.AnalyticsManager
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.common.habitica.api.HostConfig
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import java.io.InputStreamReader
import java.lang.reflect.Type
import kotlin.reflect.KCallable
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.javaField

open class HabiticaTestCase : TestCase() {
    val gson = GSonFactoryCreator.createGson()

    val apiClient: ApiClient = mockk(relaxed = true)
    val userRepository: UserRepository = mockk(relaxed = true)
    val taskRepository: TaskRepository = mockk(relaxed = true)
    val inventoryRepository: InventoryRepository = mockk(relaxed = true)
    val socialRepository: SocialRepository = mockk(relaxed = true)
    val tutorialRepository: TutorialRepository = mockk(relaxed = true)
    val appConfigManager: AppConfigManager = mockk(relaxed = true)
    val contentRepository: ContentRepository = mockk(relaxed = true)
    val userViewModel: MainUserViewModel = mockk(relaxed = true)
    val sharedPreferences: SharedPreferences = mockk(relaxed = true)
    val soundManager: SoundManager = mockk(relaxed = true)
    val notificationsManager: NotificationsManager = mockk(relaxed = true)
    val hostConfig: HostConfig = mockk(relaxed = true)
    val analyticsManager: AnalyticsManager = mockk(relaxed = true)
    val maintenanceService: MaintenanceApiService = mockk(relaxed = true)
    val tagRepository: TagRepository = mockk(relaxed = true)
    val hatchPetUseCase: HatchPetUseCase = mockk(relaxed = true)
    val feedPetUseCase: FeedPetUseCase = mockk(relaxed = true)

    val userState = MutableStateFlow<User?>(null)
    var user = User()
    lateinit var content: ContentResult

    val errorSlot = slot<Throwable>()
    val unmanagedSlot = slot<BaseObject>()

    @Before
    fun setUp() {
        clearAllMocks()

        mockkObject(MainNavigationController)

        user = loadJsonFile("user", User::class.java)
        user.stats?.lvl = 20
        user.stats?.points = 30
        every { userRepository.getUser() } returns userState
        every { userViewModel.user } returns MutableLiveData<User?>(user)
        mockkObject(ExceptionHandler)
        every { ExceptionHandler.reportError(capture(errorSlot)) } answers {
            throw errorSlot.captured
        }
        every { socialRepository.getUnmanagedCopy(capture(unmanagedSlot)) } answers { unmanagedSlot.captured }
        content = loadJsonFile("content", ContentResult::class.java)
        every { inventoryRepository.getPets() } returns flowOf(content.pets)
        every { inventoryRepository.getMounts() } returns flowOf(content.mounts)
        every { inventoryRepository.getItems(Food::class.java) } returns flowOf(content.food)
        every { inventoryRepository.getItems(Egg::class.java) } returns flowOf(content.eggs)
        every { inventoryRepository.getItems(HatchingPotion::class.java) } returns flowOf(content.hatchingPotions)
        every { inventoryRepository.getItems(QuestContent::class.java) } returns flowOf(content.quests)

        every { inventoryRepository.getItems(Food::class.java, any()) } returns flowOf(content.food)
        every { inventoryRepository.getItems(Egg::class.java, any()) } answers {
            flowOf(content.eggs)
        }
        every { inventoryRepository.getItems(HatchingPotion::class.java, any()) } returns flowOf(content.hatchingPotions)
        every { inventoryRepository.getItems(QuestContent::class.java, any()) } returns flowOf(content.quests)
    }

    internal fun <T> loadJsonFile(s: String, type: Type): T {
        val userStream = javaClass.classLoader?.getResourceAsStream("$s.json")
        return gson.fromJson(gson.newJsonReader(InputStreamReader(userStream)), type)
    }

    internal fun <C> initializeInjects(obj: C) {
        obj!!::class.java.kotlin.members.forEach {
            if (it.returnType == UserRepository::class.starProjectedType) assign(it, obj, userRepository)
            if (it.returnType == TaskRepository::class.starProjectedType) assign(it, obj, taskRepository)
            if (it.returnType == InventoryRepository::class.starProjectedType) assign(it, obj, inventoryRepository)
            if (it.returnType == SocialRepository::class.starProjectedType) assign(it, obj, socialRepository)
            if (it.returnType == TutorialRepository::class.starProjectedType) assign(it, obj, tutorialRepository)
            if (it.returnType == ContentRepository::class.starProjectedType) assign(it, obj, contentRepository)
            if (it.returnType == AppConfigManager::class.starProjectedType) assign(it, obj, appConfigManager)
            if (it.returnType == MainUserViewModel::class.starProjectedType) assign(it, obj, userViewModel)
            if (it.returnType == ApiClient::class.starProjectedType) assign(it, obj, apiClient)
            if (it.returnType == SoundManager::class.starProjectedType) assign(it, obj, soundManager)
            if (it.returnType == SharedPreferences::class.starProjectedType) assign(it, obj, sharedPreferences)
            if (it.returnType == NotificationsManager::class.starProjectedType) assign(it, obj, notificationsManager)
            if (it.returnType == HostConfig::class.starProjectedType) assign(it, obj, hostConfig)
            if (it.returnType == AnalyticsManager::class.starProjectedType) assign(it, obj, analyticsManager)
            if (it.returnType == MaintenanceApiService::class.starProjectedType) assign(it, obj, maintenanceService)
            if (it.returnType == TagRepository::class.starProjectedType) assign(it, obj, tagRepository)
            if (it.returnType == FeedPetUseCase::class.starProjectedType) assign(it, obj, feedPetUseCase)
            if (it.returnType == HatchPetUseCase::class.starProjectedType) assign(it, obj, hatchPetUseCase)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <P, C> assign(it: KCallable<*>, obj: C, value: P) {
        if ((it as KMutableProperty1<C, P>).javaField!!.get(obj) == null) {
            it.set(obj, value)
        }
    }
}
