package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.local.TaskLocalRepository
import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.shared.habitica.models.responses.TaskDirectionData
import com.habitrpg.shared.habitica.models.responses.TaskScoringResult
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.shared.habitica.models.tasks.TaskType
import com.habitrpg.shared.habitica.models.tasks.TasksOrder
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.WordSpec
import io.kotest.framework.concurrency.eventually
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.subscribers.TestSubscriber
import io.realm.Realm
import java.util.UUID

@OptIn(ExperimentalKotest::class)
class TaskRepositoryImplTest : WordSpec({
    lateinit var repository: TaskRepository
    val localRepository = mockk<TaskLocalRepository>()
    val apiClient = mockk<ApiClient>()
    beforeEach {
        val slot = slot<((Realm) -> Unit)>()
        every { localRepository.executeTransaction(transaction = capture(slot)) } answers {
            slot.captured(mockk(relaxed = true))
        }
        repository = TaskRepositoryImpl(
            localRepository,
            apiClient,
            "",
            mockk(relaxed = true),
            mockk(relaxed = true)
        )
        val liveObjectSlot = slot<BaseObject>()
        every { localRepository.getLiveObject(capture(liveObjectSlot)) } answers {
            liveObjectSlot.captured
        }
    }
    "retrieveTasks" should {
        "save tasks locally" {
            val list = TaskList()
            every { apiClient.tasks } returns Flowable.just(list)
            every { localRepository.saveTasks("", any(), any()) } returns Unit
            val order = TasksOrder()
            val subscriber = TestSubscriber<TaskList>()
            repository.retrieveTasks("", order).subscribe(subscriber)
            subscriber.assertComplete()
            verify { localRepository.saveTasks("", order, list) }
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
            every { apiClient.postTaskDirection(any(), "up") } returns Flowable.just(
                TaskDirectionData()
            )
            repository.taskChecked(user, task, true, false, null).subscribe()
            repository.taskChecked(user, task, true, false, null).subscribe()
            verify(exactly = 1) { apiClient.postTaskDirection(any(), any()) }
        }
        "get user if not passed" {
            every { apiClient.postTaskDirection(any(), "up") } returns Flowable.just(
                TaskDirectionData()
            )
            every { localRepository.getUser("") } returns Flowable.just(user)
            repository.taskChecked(null, task, true, false, null)
            eventually(5000) {
                localRepository.getUser("")
            }
        }
        "does not update user for team tasks" {
            val data = TaskDirectionData()
            data.lvl = 0
            every { apiClient.postTaskDirection(any(), "up") } returns Flowable.just(data)
            val subscriber = TestSubscriber<TaskScoringResult>()
            repository.taskChecked(user, task, true, false, null).subscribe(subscriber)
            subscriber.assertComplete()
            verify(exactly = 0) { user.stats }
            subscriber.values().first().level shouldBe null
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
            every { apiClient.postTaskDirection(any(), "up") } returns Flowable.just(data)
            val subscriber = TestSubscriber<TaskScoringResult>()
            repository.taskChecked(user, task, true, false, null).subscribe(subscriber)
            subscriber.assertComplete()
            subscriber.values().first().level shouldBe 10
            subscriber.values().first().healthDelta shouldBe 12.0
            subscriber.values().first().manaDelta shouldBe 26.0
            subscriber.values().first().hasLeveledUp shouldBe false
        }
        "set hasLeveledUp correctly" {
            val subscriber = TestSubscriber<TaskScoringResult>()
            val data = TaskDirectionData()
            data.lvl = 11
            user.stats?.lvl = 10
            every { apiClient.postTaskDirection(any(), "up") } returns Flowable.just(data)
            repository.taskChecked(user, task, true, false, null).subscribe(subscriber)

            subscriber.assertComplete()
            subscriber.values().first().level shouldBe 11
            subscriber.values().first().hasLeveledUp shouldBe true
        }
        "handle stats not being there" {
            val subscriber = TestSubscriber<TaskScoringResult>()
            val data = TaskDirectionData()
            data.lvl = 1
            user.stats = null
            every { apiClient.postTaskDirection(any(), "up") } returns Flowable.just(data)
            repository.taskChecked(user, task, true, false, null).subscribe(subscriber)
            subscriber.assertComplete()
        }
        "update daily streak" {
            val subscriber = TestSubscriber<TaskScoringResult>()
            val data = TaskDirectionData()
            data.delta = 1.0f
            data.lvl = 1
            task.type = TaskType.DAILY
            task.value = 0.0
            every { apiClient.postTaskDirection(any(), "up") } returns Flowable.just(data)
            repository.taskChecked(user, task, true, false, null).subscribe(subscriber)

            subscriber.assertComplete()
            task.streak shouldBe 1
            task.completed shouldBe true
        }
        "update habit counter" {
            val subscriber = TestSubscriber<TaskScoringResult>()
            val data = TaskDirectionData()
            data.delta = 1.0f
            data.lvl = 1
            task.type = TaskType.HABIT
            task.value = 0.0
            every { apiClient.postTaskDirection(any(), "up") } returns Flowable.just(data)
            repository.taskChecked(user, task, true, false, null).subscribe(subscriber)
            subscriber.assertComplete()
            task.counterUp shouldBe 1

            data.delta = -10.0f
            every { apiClient.postTaskDirection(any(), "down") } returns Flowable.just(data)
            val downSubscriber = TestSubscriber<TaskScoringResult>()
            repository.taskChecked(user, task, false, true, null).subscribe(downSubscriber)
            downSubscriber.assertComplete()
            task.counterUp shouldBe 1
            task.counterDown shouldBe 1
        }
    }
    afterEach { clearAllMocks() }
})
