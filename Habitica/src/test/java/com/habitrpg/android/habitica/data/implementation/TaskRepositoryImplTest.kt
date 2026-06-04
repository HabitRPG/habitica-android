package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.local.TaskLocalRepository
import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import com.habitrpg.android.habitica.widget.WidgetUpdater
import com.habitrpg.shared.habitica.models.responses.TaskDirectionData
import com.habitrpg.shared.habitica.models.tasks.TaskType
import com.habitrpg.shared.habitica.models.tasks.TasksOrder
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import io.realm.Realm
import kotlinx.coroutines.flow.flowOf
import java.util.Date
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalKotest::class)
class TaskRepositoryImplTest : WordSpec({
    lateinit var repository: TaskRepository
    val localRepository = mockk<TaskLocalRepository>()
    val apiClient = mockk<ApiClient>()
    val widgetUpdater = mockk<WidgetUpdater>(relaxed = true)
    beforeEach {
        val slot = slot<((Realm) -> Unit)>()
        every { localRepository.executeTransaction(transaction = capture(slot)) } answers {
            slot.captured(mockk(relaxed = true))
        }
        val authenticationHandler = mockk<AuthenticationHandler>()
        every { authenticationHandler.currentUserID } answers {
            ""
        }
        repository =
            TaskRepositoryImpl(
                localRepository,
                apiClient,
                authenticationHandler,
                mockk(relaxed = true),
                widgetUpdater
            )
        val liveObjectSlot = slot<BaseObject>()
        every { localRepository.getLiveObject(capture(liveObjectSlot)) } answers {
            liveObjectSlot.captured
        }
    }
    "retrieveTasks" should {
        "save tasks locally" {
            val list = TaskList()
            coEvery { apiClient.getTasks() } returns list
            every { localRepository.saveTasks("", any(), any()) } returns Unit
            val order = TasksOrder()
            repository.retrieveTasks("", order)
            verify { localRepository.saveTasks("", order, list) }
            verify { widgetUpdater.updateTaskListWidgets() }
        }
        "not refresh widgets when task retrieval has no data" {
            coEvery { apiClient.getTasks() } returns null
            val order = TasksOrder()
            val result = repository.retrieveTasks("", order)
            result shouldBe null
            verify(exactly = 0) { localRepository.saveTasks(any(), any(), any()) }
            verify(exactly = 0) { widgetUpdater.updateTaskListWidgets() }
        }
        "refresh task list widgets when saving tasks directly" {
            val list = TaskList()
            val order = TasksOrder()
            every { localRepository.saveTasks("user-id", any(), any()) } returns Unit
            repository.saveTasks("user-id", order, list)
            verify { localRepository.saveTasks("user-id", order, list) }
            verify { widgetUpdater.updateTaskListWidgets() }
        }
        "refresh task list widgets when saving fetched dailies for a due date" {
            val list = TaskList()
            val order = TasksOrder()
            val dueDate = Date()
            coEvery { apiClient.getTasks("dailys", any()) } returns list
            every { localRepository.saveTasks("user-id", any(), any()) } returns Unit

            repository.retrieveTasks("user-id", order, dueDate)

            verify { localRepository.saveTasks("user-id", order, list) }
            verify { widgetUpdater.updateTaskListWidgets() }
        }
    }
    "local task mutations" should {
        "refresh task list widgets after direct task writes" {
            val task = Task().apply { id = "task-id" }
            every { localRepository.save(any<Task>()) } returns Unit
            every { localRepository.markTaskCompleted("task-id", true) } returns Unit
            every { localRepository.swapTaskPosition(1, 2) } returns Unit

            repository.saveTask(task)
            repository.markTaskCompleted("task-id", true)
            repository.swapTaskPosition(1, 2)

            verify(exactly = 3) { widgetUpdater.updateTaskListWidgets() }
        }

        "refresh task list widgets after task position updates" {
            val task = Task().apply { id = "task-id" }
            val positions = listOf("task-id")
            every { localRepository.getTask("task-id") } returns flowOf(task)
            coEvery { apiClient.postTaskNewPosition("task-id", 4) } returns positions
            every { localRepository.updateTaskPositions(positions) } returns Unit

            repository.updateTaskPosition(TaskType.TODO, "task-id", 4)

            verify { localRepository.updateTaskPositions(positions) }
            verify { widgetUpdater.updateTaskListWidgets() }
        }

        "refresh task list widgets after optimistic task creation writes" {
            val task = Task().apply { id = "local-task-id" }
            val savedTask = Task().apply { id = "server-task-id" }
            every { localRepository.save(any<Task>()) } returns Unit
            coEvery { apiClient.createTask(any()) } returns savedTask

            repository.createTask(task, force = true)

            verify(atLeast = 1) { widgetUpdater.updateTaskListWidgets() }
        }

        "refresh task list widgets after optimistic task update writes" {
            val task = Task().apply { id = "task-id" }
            val savedTask = Task().apply { id = "task-id" }
            every { localRepository.getUnmanagedCopy(task) } returns task
            every { localRepository.save(any<Task>()) } returns Unit
            coEvery { apiClient.updateTask("task-id", task) } returns savedTask

            repository.updateTask(task, force = true)

            verify(atLeast = 1) { widgetUpdater.updateTaskListWidgets() }
        }
    }
    "taskChecked" should {
        val task = Task()
        task.id = UUID.randomUUID().toString()
        lateinit var user: User
        beforeEach {
            user = spyk(User())
            user.stats = Stats()
        }
        "debounce" {
            coEvery { apiClient.postTaskDirection(any(), "up") } returns TaskDirectionData()
            repository.taskChecked(user, task, true, false, null)
            repository.taskChecked(user, task, true, false, null)
            coVerify(exactly = 1) { apiClient.postTaskDirection(any(), any()) }
        }
        "get user if not passed" {
            coEvery { apiClient.postTaskDirection(any(), "up") } returns TaskDirectionData()
            coEvery { localRepository.getUser("") } returns flowOf(user)
            repository.taskChecked(null, task, true, false, null)
            eventually(5000.milliseconds) {
                localRepository.getUser("")
            }
        }
        "builds task result correctly" {
            val data = TaskDirectionData()
            data.lvl = 10
            data.hp = 20.0
            data.mp = 30.0
            data.gp = 40.0
            user.stats?.lvl = 10
            user.stats?.hp = 8.0
            user.stats?.mp = 4.0
            coEvery { apiClient.postTaskDirection(any(), "up") } returns data
            val result = repository.taskChecked(user, task, true, false, null)
            result?.level shouldBe 10
            result?.healthDelta shouldBe 12.0
            result?.manaDelta shouldBe 26.0
            result?.hasLeveledUp shouldBe false
            verify { widgetUpdater.updateAllWidgets() }
        }
        "set hasLeveledUp correctly" {
            val data = TaskDirectionData()
            data.lvl = 11
            user.stats?.lvl = 10
            coEvery { apiClient.postTaskDirection(any(), "up") } returns data
            val result = repository.taskChecked(user, task, true, false, null)
            result?.level shouldBe 11
            result?.hasLeveledUp shouldBe true
        }
        "handle stats not being there" {
            val data = TaskDirectionData()
            data.lvl = 1
            user.stats = null
            coEvery { apiClient.postTaskDirection(any(), "up") } returns data
            repository.taskChecked(user, task, true, false, null)
        }
        "update daily streak" {
            val data = TaskDirectionData()
            data.delta = 1.0f
            data.lvl = 1
            task.type = TaskType.DAILY
            task.value = 0.0
            coEvery { apiClient.postTaskDirection(any(), "up") } returns data
            repository.taskChecked(user, task, true, false, null)
            task.streak shouldBe 1
            task.completed shouldBe true
        }
        "update habit counter" {
            val data = TaskDirectionData()
            data.delta = 1.0f
            data.lvl = 1
            task.type = TaskType.HABIT
            task.value = 0.0
            coEvery { apiClient.postTaskDirection(any(), "up") } returns data
            repository.taskChecked(user, task, true, false, null)
            task.counterUp shouldBe 1

            data.delta = -10.0f
            coEvery { apiClient.postTaskDirection(any(), "down") } returns data
            repository.taskChecked(user, task, false, true, null)
            task.counterUp shouldBe 1
            task.counterDown shouldBe 1
        }
    }
    afterEach { clearAllMocks() }
})
