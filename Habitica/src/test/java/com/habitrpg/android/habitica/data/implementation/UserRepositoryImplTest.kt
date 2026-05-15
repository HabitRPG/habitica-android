package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.data.local.UserLocalRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.models.responses.SkillResponse
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.models.user.Preferences
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import com.habitrpg.android.habitica.widget.WidgetUpdater
import com.habitrpg.shared.habitica.models.tasks.TasksOrder
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import java.util.GregorianCalendar
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImplTest : WordSpec({
    lateinit var repository: UserRepository
    val localRepository = mockk<UserLocalRepository>(relaxed = true)
    val apiClient = mockk<ApiClient>()
    val taskRepository = mockk<TaskRepository>(relaxed = true)
    val widgetUpdater = mockk<WidgetUpdater>(relaxed = true)
    val mainDispatcher = UnconfinedTestDispatcher()

    beforeEach {
        Dispatchers.setMain(mainDispatcher)
        repository =
            UserRepositoryImpl(
                localRepository,
                apiClient,
                AuthenticationHandler("user-id"),
                taskRepository,
                mockk<AppConfigManager>(relaxed = true),
                widgetUpdater
            )
        every { localRepository.saveUser(any(), any()) } just runs
    }

    "retrieveUser" should {
        "refresh widgets after saving fetched user data" {
            val user = fetchedUser()
            coEvery { apiClient.retrieveUser(false) } returns user

            val result = repository.retrieveUser(withTasks = false, forced = true)

            result shouldBe user
            verify { localRepository.saveUser(user) }
            verify { widgetUpdater.updateAllWidgets() }
            verify(exactly = 0) { taskRepository.saveTasks(any(), any(), any()) }
        }

        "save fetched tasks and refresh widgets when task data is included" {
            val tasks = TaskList()
            val tasksOrder = TasksOrder()
            val user = fetchedUser().apply {
                this.tasks = tasks
                this.tasksOrder = tasksOrder
            }
            coEvery { apiClient.retrieveUser(true) } returns user

            val result = repository.retrieveUser(withTasks = true, forced = true)

            result shouldBe user
            verify { localRepository.saveUser(user) }
            verify { taskRepository.saveTasks("user-id", tasksOrder, tasks) }
            verify { widgetUpdater.updateAllWidgets() }
        }

        "not refresh widgets when no user data is returned" {
            coEvery { apiClient.retrieveUser(false) } returns null

            val result = repository.retrieveUser(withTasks = false, forced = true)

            result shouldBe null
            verify(exactly = 0) { localRepository.saveUser(any(), any()) }
            verify(exactly = 0) { taskRepository.saveTasks(any(), any(), any()) }
            verify(exactly = 0) { widgetUpdater.updateAllWidgets() }
        }
    }

    "local user mutations" should {
        "refresh widgets after saving synced stats" {
            val user = fetchedUser().apply {
                stats = Stats().apply {
                    toNextLevel = 10
                    maxMP = 10
                }
            }
            coEvery { apiClient.syncUserStats() } returns user

            val result = repository.syncUserStats()

            result shouldBe user
            verify { localRepository.saveUser(user) }
            verify { widgetUpdater.updateAllWidgets() }
        }

        "refresh widgets after updateUser merges fetched user data" {
            val oldUser = fetchedUser()
            val newUser = fetchedUser().apply {
                stats = Stats().apply { hp = 40.0 }
            }
            every { localRepository.getUser("user-id") } returns flowOf(oldUser)
            coEvery { apiClient.updateUser(mapOf("stats.hp" to 40.0)) } returns newUser

            val result = repository.updateUser("stats.hp", 40.0)

            result shouldBe oldUser
            verify { localRepository.saveUser(oldUser, false) }
            verify { widgetUpdater.updateAllWidgets() }
        }

        "refresh widgets after skill responses merge user data" {
            val oldUser = fetchedUser().apply {
                stats = Stats().apply { hp = 30.0 }
            }
            val newUser = fetchedUser().apply {
                stats = Stats().apply { hp = 25.0 }
            }
            val response = SkillResponse().apply { user = newUser }
            every { localRepository.getUser("user-id") } returns flowOf(oldUser)
            every { localRepository.getLiveObject(oldUser) } returns oldUser
            coEvery { apiClient.useSkill("heal", "self") } returns response

            val result = repository.useSkill("heal", "self")

            result shouldBe response
            verify { localRepository.saveUser(oldUser, false) }
            verify { widgetUpdater.updateAllWidgets() }
        }
    }

    afterEach {
        Dispatchers.resetMain()
        clearAllMocks()
    }
})

private fun fetchedUser(): User {
    return User().apply {
        id = "user-id"
        preferences = Preferences().apply {
            timezoneOffset = currentTimezoneOffset()
        }
    }
}

private fun currentTimezoneOffset(): Int {
    val calendar = GregorianCalendar()
    return -TimeUnit.MINUTES.convert(
        calendar.timeZone.getOffset(calendar.timeInMillis).toLong(),
        TimeUnit.MILLISECONDS
    ).toInt()
}
