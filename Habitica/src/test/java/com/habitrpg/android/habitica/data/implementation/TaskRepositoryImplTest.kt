package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TagRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.local.TaskLocalRepository
import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AuthenticationHandler
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalKotest::class, ExperimentalCoroutinesApi::class)
class TaskRepositoryImplTest :
    WordSpec({
        lateinit var repository: TaskRepository
        val localRepository = mockk<TaskLocalRepository>()
        val apiClient = mockk<ApiClient>()
        val tagRepository = mockk<TagRepository>()
        beforeEach {
            every { localRepository.getTasksWithTaskId(any()) } returns listOf()
            every { localRepository.handleTaskResponse(any(), any(), any(), any(), any()) } answers {
                val task = thirdArg<Task>()
                val up = arg<Boolean>(3)
                when (task.type) {
                    TaskType.DAILY -> task.streak = (task.streak ?: 0) + if (up) 1 else -1
                    TaskType.HABIT ->
                        if (up) {
                            task.counterUp = (task.counterUp ?: 0) + 1
                        } else {
                            task.counterDown = (task.counterDown ?: 0) + 1
                        }
                    else -> {}
                }
                if (task.type == TaskType.DAILY || task.type == TaskType.TODO) {
                    task.completeForUser(firstArg<User>().id, up)
                }
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
                    tagRepository,
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
            }

            "resolve thin, id-only tag placeholders against locally known tags before saving" {
                val task =
                    Task().apply {
                        id = "task-1"
                        tags?.add(Tag().apply { id = "tag-1" })
                        tags?.add(Tag().apply { id = "tag-unknown" })
                    }
                val list = TaskList().apply { tasks = mutableMapOf("task-1" to task) }
                val knownTag = Tag().apply { id = "tag-1"; name = "Work" }
                coEvery { apiClient.getTasks() } returns list
                coEvery { tagRepository.getTags("") } returns flowOf(listOf(knownTag))
                every { localRepository.saveTasks("", any(), any()) } returns Unit
                repository.retrieveTasks("", TasksOrder())
                task.tags?.map { it.id } shouldBe listOf("tag-1")
                task.tags?.firstOrNull()?.name shouldBe "Work"
            }

            "leave already-full tags untouched and skip tag lookup entirely" {
                val task =
                    Task().apply {
                        id = "task-1"
                        tags?.add(Tag().apply { id = "tag-1"; name = "Work" })
                    }
                val list = TaskList().apply { tasks = mutableMapOf("task-1" to task) }
                coEvery { apiClient.getTasks() } returns list
                every { localRepository.saveTasks("", any(), any()) } returns Unit
                repository.retrieveTasks("", TasksOrder())
                coVerify(exactly = 0) { tagRepository.getTags(any()) }
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
