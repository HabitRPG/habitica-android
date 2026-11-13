package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TagRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.local.TaskLocalRepository
import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.GroupAssignedDetails
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskGroupPlan
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
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import io.realm.RealmList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import java.util.Date
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
                    TaskType.DAILY -> {
                        task.streak = (task.streak ?: 0) + if (up) 1 else -1
                    }

                    TaskType.HABIT -> {
                        if (up) {
                            task.counterUp = (task.counterUp ?: 0) + 1
                        } else {
                            task.counterDown = (task.counterDown ?: 0) + 1
                        }
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
                val knownTag =
                    Tag().apply {
                        id = "tag-1"
                        name = "Work"
                    }
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
                        tags?.add(
                            Tag().apply {
                                id = "tag-1"
                                name = "Work"
                            },
                        )
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
        "getTasks" should {
            "use the current user if no user is given" {
                val tasks = listOf(Task())
                every { localRepository.getTasks(TaskType.HABIT, "", emptyArray()) } returns flowOf(tasks)
                repository.getTasks(TaskType.HABIT, null, emptyArray()).first() shouldBe tasks
            }
        }
        "retrieveCompletedTodos" should {
            "save completed todos locally" {
                val task = Task().apply { id = "task-1" }
                val list = TaskList().apply { tasks = mutableMapOf("task-1" to task) }
                coEvery { apiClient.getTasks("completedTodos") } returns list
                every { localRepository.saveCompletedTodos("user-1", any()) } returns Unit
                repository.retrieveCompletedTodos("user-1") shouldBe list
                verify { localRepository.saveCompletedTodos("user-1", match { it.single() == task }) }
            }

            "not save anything if the request failed" {
                coEvery { apiClient.getTasks("completedTodos") } returns null
                repository.retrieveCompletedTodos("user-1") shouldBe null
                verify(exactly = 0) { localRepository.saveCompletedTodos(any(), any()) }
            }
        }
        "retrieveTasks with a due date" should {
            "load and save the dailies for that date" {
                val list = TaskList()
                coEvery { apiClient.getTasks("dailys", any()) } returns list
                every { localRepository.saveTasks("user-1", any(), list) } returns Unit
                repository.retrieveTasks("user-1", TasksOrder(), Date(0)) shouldBe list
                coVerify { apiClient.getTasks("dailys", match { it.startsWith("19") }) }
            }

            "return the dailies without saving them" {
                val list = TaskList()
                coEvery { apiClient.getTasks("dailys", any()) } returns list
                repository.retrieveDailiesFromDate(Date(0)) shouldBe list
                verify(exactly = 0) { localRepository.saveTasks(any(), any(), any()) }
            }
        }
        "scoreChecklistItem" should {
            "save the scored checklist item" {
                val item = ChecklistItem("item-1", "Item", true)
                val task = Task().apply { checklist = RealmList(ChecklistItem("item-2"), item) }
                coEvery { apiClient.scoreChecklistItem("task-1", "item-1") } returns task
                every { localRepository.save(item) } returns Unit
                repository.scoreChecklistItem("task-1", "item-1") shouldBe task
                verify { localRepository.save(item) }
            }
        }
        "createTask" should {
            beforeEach { every { localRepository.save(any<Task>()) } returns Unit }

            "assign an id and save the created task with its tags" {
                val tag = Tag().apply { id = "tag-1" }
                val task = Task().apply { tags = RealmList(tag) }
                val created = Task()
                coEvery { apiClient.createTask(task) } returns created
                repository.createTask(task, false) shouldBe created
                task.id shouldNotBe null
                task.isCreating shouldBe true
                created.tags shouldBe task.tags
                created.dateCreated shouldNotBe null
                verify { localRepository.save(created) }
            }

            "create group tasks in their group" {
                val task = Task().apply { group = TaskGroupPlan().apply { groupID = "group-1" } }
                coEvery { apiClient.createGroupTask("group-1", task) } returns Task()
                repository.createTask(task, false)
                task.ownerID shouldBe "group-1"
                coVerify(exactly = 0) { apiClient.createTask(any()) }
            }

            "mark the task as errored if creating failed" {
                val task = Task()
                coEvery { apiClient.createTask(task) } returns null
                repository.createTask(task, false) shouldBe null
                task.hasErrored shouldBe true
                task.isSaving shouldBe false
            }

            "ignore a second task created right after the first one" {
                coEvery { apiClient.createTask(any()) } returns Task()
                repository.createTask(Task(), false)
                repository.createTask(Task(), false) shouldBe null
                repository.createTask(Task(), true)
                coVerify(exactly = 2) { apiClient.createTask(any()) }
            }
        }
        "updateTask" should {
            beforeEach {
                every { localRepository.save(any<Task>()) } returns Unit
                every { localRepository.getUnmanagedCopy(any<Task>()) } answers { firstArg() }
            }

            "keep the local position, id and owner of the updated task" {
                val task =
                    Task().apply {
                        id = "task-1"
                        position = 3
                        ownerID = "user-1"
                    }
                val updated = Task()
                coEvery { apiClient.updateTask("task-1", task) } returns updated
                repository.updateTask(task, false) shouldBe updated
                updated.id shouldBe "task-1"
                updated.position shouldBe 3
                updated.ownerID shouldBe "user-1"
                verify { localRepository.save(updated) }
            }

            "mark the task as errored if updating failed" {
                val task = Task().apply { id = "task-1" }
                coEvery { apiClient.updateTask("task-1", task) } returns null
                repository.updateTask(task, false) shouldBe null
                task.hasErrored shouldBe true
            }

            "return the task unchanged if it has no id" {
                val task = Task()
                repository.updateTask(task, false) shouldBe task
                coVerify(exactly = 0) { apiClient.updateTask(any(), any()) }
            }
        }
        "markTaskNeedsWork" should {
            "reset the completion of the user and save the task" {
                val details =
                    GroupAssignedDetails().apply {
                        assignedUserID = "user-2"
                        completed = true
                        completedDate = Date()
                    }
                val saved = Task().apply { group = TaskGroupPlan().apply { assignedUsersDetail = RealmList(details) } }
                coEvery { apiClient.markTaskNeedsWork("task-1", "user-2") } returns saved
                every { localRepository.save(saved) } returns Unit
                repository.markTaskNeedsWork(Task().apply { id = "task-1"; position = 2 }, "user-2")
                details.completed shouldBe false
                details.completedDate shouldBe null
                saved.position shouldBe 2
                verify { localRepository.save(saved) }
            }
        }
        "updateTaskPosition" should {
            "save the new positions of personal tasks" {
                every { localRepository.getTask("task-1") } returns flowOf(Task())
                coEvery { apiClient.postTaskNewPosition("task-1", 2) } returns listOf("task-2", "task-1")
                every { localRepository.updateTaskPositions(listOf("task-2", "task-1")) } returns Unit
                repository.updateTaskPosition(TaskType.TODO, "task-1", 2) shouldBe listOf("task-2", "task-1")
                verify { localRepository.updateTaskPositions(listOf("task-2", "task-1")) }
            }

            "move group tasks through the group endpoint" {
                every { localRepository.getTask("task-1") } returns flowOf(Task().apply { group = TaskGroupPlan().apply { groupID = "group-1" } })
                coEvery { apiClient.postGroupTaskNewPosition("task-1", 0) } returns null
                repository.updateTaskPosition(TaskType.TODO, "task-1", 0) shouldBe null
                verify(exactly = 0) { localRepository.updateTaskPositions(any()) }
            }
        }
        "syncErroredTasks" should {
            "create unsaved tasks and update the others" {
                val unsaved = Task().apply { isCreating = true }
                val changed = Task().apply { id = "task-2" }
                every { localRepository.getErroredTasks("") } returns flowOf(listOf(unsaved, changed))
                every { localRepository.getUnmanagedCopy(any<Task>()) } answers { firstArg() }
                every { localRepository.save(any<Task>()) } returns Unit
                coEvery { apiClient.createTask(unsaved) } returns Task()
                coEvery { apiClient.updateTask("task-2", changed) } returns Task()
                repository.syncErroredTasks()?.size shouldBe 2
            }
        }
        "createTaskInBackground" should {
            "apply assignment changes and notify when the task was saved" {
                Dispatchers.setMain(UnconfinedTestDispatcher())
                val task = Task().apply { id = "task-1"; ownerID = "group-1"; group = TaskGroupPlan().apply { groupID = "group-1" } }
                val created = Task().apply { id = "task-1"; ownerID = "group-1" }
                every { localRepository.save(any<Task>()) } returns Unit
                coEvery { apiClient.createGroupTask("group-1", task) } returns created
                coEvery { apiClient.assignToTask("task-1", listOf("user-2")) } returns Task()
                coEvery { apiClient.unassignFromTask("task-1", "user-3") } returns Task()
                every { localRepository.getTasks("group-1") } returns flowOf(listOf(created))
                var completed = false
                repository.createTaskInBackground(
                    task,
                    mapOf("assign" to mutableListOf("user-2"), "unassign" to mutableListOf("user-3")),
                ) { completed = true }
                eventually(5000.milliseconds) { completed shouldBe true }
                coVerify { apiClient.assignToTask("task-1", listOf("user-2")) }
                coVerify { apiClient.unassignFromTask("task-1", "user-3") }
            }
        }
        "delegating methods" should {
            "pass calls on to the api and local repository" {
                coEvery { apiClient.createTasks(any()) } returns emptyList()
                coEvery { apiClient.unlinkAllTasks("challenge-1", "keep-all") } returns null
                coEvery { apiClient.bulkScoreTasks(any()) } returns null
                every { localRepository.markTaskCompleted("task-1", true) } returns Unit
                every { localRepository.swapTaskPosition(1, 2) } returns Unit
                every { localRepository.getTasksForChallenge("challenge-1", "") } returns flowOf(emptyList())
                repository.createTasks(emptyList())
                repository.unlinkAllTasks("challenge-1", "keep-all")
                repository.bulkScoreTasks(emptyList())
                repository.markTaskCompleted("task-1", true)
                repository.swapTaskPosition(1, 2)
                repository.getTasksForChallenge("challenge-1").first() shouldBe emptyList()
                coVerify { apiClient.unlinkAllTasks("challenge-1", "keep-all") }
                verify { localRepository.markTaskCompleted("task-1", true) }
                verify { localRepository.swapTaskPosition(1, 2) }
            }
        }
        afterEach { clearAllMocks() }
    })
