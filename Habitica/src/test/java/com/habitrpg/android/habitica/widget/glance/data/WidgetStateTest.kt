package com.habitrpg.android.habitica.widget.glance.data

import android.content.Context
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.Preferences
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.models.user.UserTaskPreferences
import com.habitrpg.android.habitica.widget.glance.work.CronBoundaryRefreshWorker
import com.habitrpg.shared.habitica.models.tasks.TaskType
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.realm.RealmList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class WidgetStateTest :
    WordSpec({
        val context = mockk<Context>(relaxed = true)

        beforeSpec { Locale.setDefault(Locale.US) }

        fun millisAt(
            day: Int,
            hour: Int,
            minute: Int = 0,
        ) = Calendar
            .getInstance()
            .apply {
                clear()
                set(2026, Calendar.MARCH, day, hour, minute)
            }.timeInMillis

        "lastBoundaryMillis" should {
            "use today's day start once it passed" {
                CronBoundaryRefreshWorker.lastBoundaryMillis(4, millisAt(10, 9)) shouldBe millisAt(10, 4, 2)
            }

            "use yesterday's day start before it is reached" {
                CronBoundaryRefreshWorker.lastBoundaryMillis(4, millisAt(10, 3)) shouldBe millisAt(9, 4, 2)
            }

            "wait for the buffer after the day start" {
                CronBoundaryRefreshWorker.lastBoundaryMillis(4, millisAt(10, 4, 1)) shouldBe millisAt(9, 4, 2)
            }

            "treat a day start of 24 as midnight" {
                CronBoundaryRefreshWorker.lastBoundaryMillis(24, millisAt(10, 9)) shouldBe millisAt(10, 0, 2)
            }
        }

        "nextBoundaryMillis" should {
            "use today's day start before it is reached" {
                CronBoundaryRefreshWorker.nextBoundaryMillis(4, millisAt(10, 3)) shouldBe millisAt(10, 4, 2)
            }

            "use tomorrow's day start once it passed" {
                CronBoundaryRefreshWorker.nextBoundaryMillis(4, millisAt(10, 9)) shouldBe millisAt(11, 4, 2)
            }

            "clamp invalid day starts" {
                CronBoundaryRefreshWorker.nextBoundaryMillis(-3, millisAt(10, 9)) shouldBe millisAt(11, 0, 2)
            }
        }

        "computeNeedsCron" should {
            fun user(
                lastCron: Date?,
                dayStart: Int = 0,
            ) = User().apply {
                this.lastCron = lastCron
                preferences = Preferences().apply { this.dayStart = dayStart }
            }

            "be false without a user" {
                computeNeedsCron(null) shouldBe false
            }

            "be true when the server flagged it" {
                User().apply { needsCron = true }.let { computeNeedsCron(it) } shouldBe true
            }

            "be false without a last cron" {
                computeNeedsCron(user(null)) shouldBe false
            }

            "be false if cron ran after the last day start" {
                computeNeedsCron(user(Date(millisAt(10, 5)), dayStart = 4), now = millisAt(10, 9)) shouldBe false
            }

            "be true if cron ran before the last day start" {
                computeNeedsCron(user(Date(millisAt(9, 23)), dayStart = 4), now = millisAt(10, 9)) shouldBe true
            }

            "respect a custom day start" {
                // Cron at 1am is still the previous day for a day start of 4
                computeNeedsCron(user(Date(millisAt(10, 1)), dayStart = 4), now = millisAt(10, 3)) shouldBe false
                computeNeedsCron(user(Date(millisAt(10, 1)), dayStart = 0), now = millisAt(10, 3)) shouldBe false
                computeNeedsCron(user(Date(millisAt(9, 23)), dayStart = 0), now = millisAt(10, 3)) shouldBe true
            }
        }

        "toWidgetItem" should {
            "copy the task details and count the checklist" {
                val task =
                    Task().apply {
                        id = "task-1"
                        text = "Floss"
                        value = 3.5
                        checklist = RealmList(ChecklistItem(completed = true), ChecklistItem(), ChecklistItem(completed = true))
                    }
                task.toWidgetItem() shouldBe TaskWidgetItem("task-1", "Floss", 3.5, 3, 2)
            }

            "handle tasks without id and checklist" {
                val task = Task().apply { checklist = null }
                task.toWidgetItem() shouldBe TaskWidgetItem("", "", 0.0, 0, 0)
            }
        }

        "StatsWidgetState.fromUser" should {

            fun user(configure: Stats.() -> Unit = {}) =
                User().apply {
                    id = "user-1"
                    balance = 5.0
                    preferences = Preferences()
                    stats =
                        Stats().apply {
                            hp = 42.7
                            maxHealth = 50
                            exp = 120.0
                            toNextLevel = 300
                            mp = 30.0
                            maxMP = 80
                            gp = 250.9
                            lvl = 12
                            habitClass = Stats.WARRIOR
                            configure()
                        }
                }

            "be null without stats" {
                StatsWidgetState.fromUser(context, User()).shouldBeNull()
            }

            "map the stats" {
                val state = StatsWidgetState.fromUser(context, user(), "/cache/avatar.png").shouldNotBeNull()
                state.hpText shouldBe "42"
                state.maxHpText shouldBe "50"
                state.expText shouldBe "120"
                state.toNextLevelText shouldBe "300"
                state.mpText shouldBe "30"
                state.maxMpText shouldBe "80"
                state.level shouldBe 12
                state.goldText shouldBe "250"
                state.gemsText shouldBe "20"
                state.className shouldBe Stats.WARRIOR
                state.avatarBitmapPath shouldBe "/cache/avatar.png"
                state.userId shouldBe "user-1"
            }

            "show mana for classes from level 10" {
                StatsWidgetState.fromUser(context, user())?.showMp shouldBe true
                StatsWidgetState.fromUser(context, user { lvl = 9 })?.showMp shouldBe false
                StatsWidgetState.fromUser(context, user { habitClass = null })?.showMp shouldBe false
            }

            "hide class and mana if classes are disabled" {
                val user = user().apply { preferences?.disableClasses = true }
                val state = StatsWidgetState.fromUser(context, user).shouldNotBeNull()
                state.showMp shouldBe false
                state.className.shouldBeNull()
            }
        }

        "loading widget state" should {
            val userRepository = mockk<UserRepository>()
            val taskRepository = mockk<TaskRepository>(relaxed = true)
            val entryPoint =
                mockk<WidgetEntryPoint> {
                    every { userRepository() } returns userRepository
                    every { taskRepository() } returns taskRepository
                }
            val user =
                User().apply {
                    id = "user-1"
                    preferences = Preferences().apply { tasks = UserTaskPreferences().apply { mirrorGroupTasks = RealmList("group-1") } }
                }

            fun task(
                id: String,
                completed: Boolean = false,
                isDue: Boolean? = true,
            ) = Task().apply {
                this.id = id
                this.completed = completed
                this.isDue = isDue
            }

            beforeEach {
                mockkStatic(::widgetEntryPoint)
                every { widgetEntryPoint(any()) } returns entryPoint
                Dispatchers.setMain(UnconfinedTestDispatcher())
                every { userRepository.getUser() } returns flowOf(user)
            }
            afterEach {
                Dispatchers.resetMain()
                unmockkStatic(::widgetEntryPoint)
                clearMocks(taskRepository, userRepository)
            }

            "show open dailies that are due" {
                every { taskRepository.getTasks(TaskType.DAILY, "user-1", match { it.contentEquals(arrayOf("group-1")) }) } returns
                    flowOf(listOf(task("due"), task("done", completed = true), task("not-due", isDue = false)))
                val state = loadTaskListStateOrNull(context, TaskType.DAILY).shouldNotBeNull()
                state.tasks.map { it.id } shouldContainExactly listOf("due")
                verify { taskRepository.refreshLocalData() }
            }

            "show all open todos regardless of due state" {
                every { taskRepository.getTasks(TaskType.TODO, "user-1", match { it.contentEquals(arrayOf("group-1")) }) } returns
                    flowOf(listOf(task("todo", isDue = null), task("done", completed = true)))
                val state = loadTaskListStateOrNull(context, TaskType.TODO).shouldNotBeNull()
                state.tasks.map { it.id } shouldContainExactly listOf("todo")
            }

            "count completed dailies that are due" {
                every { taskRepository.getTasks(TaskType.DAILY, "user-1", match { it.contentEquals(arrayOf("group-1")) }) } returns
                    flowOf(listOf(task("due"), task("done", completed = true), task("not-due", isDue = false)))
                val state = loadDailyCountStateOrNull(context).shouldNotBeNull()
                state.totalDue shouldBe 2
                state.completed shouldBe 1
            }

            "be null without a user" {
                every { userRepository.getUser() } returns flowOf(null)
                loadTaskListStateOrNull(context, TaskType.DAILY).shouldBeNull()
                loadDailyCountStateOrNull(context).shouldBeNull()
            }

            "be null if loading fails" {
                every { taskRepository.getTasks(any(), any(), any()) } throws IllegalStateException("Realm closed")
                loadTaskListStateOrNull(context, TaskType.DAILY).shouldBeNull()
                loadDailyCountStateOrNull(context).shouldBeNull()
                verify(exactly = 2) { taskRepository.getTasks(any(), any(), any()) }
            }
        }
    })
