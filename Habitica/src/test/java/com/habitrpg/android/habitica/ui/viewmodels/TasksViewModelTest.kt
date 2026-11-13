package com.habitrpg.android.habitica.ui.viewmodels

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.data.TagRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.TaskAlarmManager
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.TeamPlan
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskGroupPlan
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.shared.habitica.models.tasks.TaskType
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.realm.RealmList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModelTest :
    WordSpec({
        val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
        val userRepository = mockk<UserRepository>(relaxed = true)
        val mainUserViewModel = mockk<MainUserViewModel>()
        val taskRepository = mockk<TaskRepository>(relaxed = true)
        val tagRepository = mockk<TagRepository>()
        val appConfigManager = mockk<AppConfigManager>()
        val sharedPreferences = mockk<SharedPreferences>(relaxed = true)
        val contentRepository = mockk<ContentRepository>(relaxed = true)
        val taskAlarmManager = mockk<TaskAlarmManager>(relaxed = true)
        val context = mockk<Context>(relaxed = true)
        val teamPlans = MutableStateFlow<List<TeamPlan>>(emptyList())

        every { mainUserViewModel.user } returns MutableLiveData(User())
        every { mainUserViewModel.userID } returns "user-1"
        every { mainUserViewModel.displayName } returns "Me"
        every { userRepository.getTeamPlans() } returns teamPlans

        fun makeViewModel() =
            TasksViewModel(
                userRepository,
                mainUserViewModel,
                taskRepository,
                tagRepository,
                appConfigManager,
                sharedPreferences,
                contentRepository,
                taskAlarmManager,
                context,
            )

        fun task(
            type: TaskType,
            completed: Boolean = false,
            value: Double = 0.0,
            isDue: Boolean? = null,
            tagIDs: List<String> = emptyList(),
        ) = Task().apply {
            this.type = type
            this.completed = completed
            this.value = value
            this.isDue = isDue
            tags = RealmList(*tagIDs.map { Tag().apply { id = it } }.toTypedArray())
        }

        fun teamPlan(
            id: String,
            summary: String,
        ) = TeamPlan().apply {
            this.id = id
            this.summary = summary
        }

        beforeEach {
            Dispatchers.setMain(testDispatcher)
            teamPlans.value = emptyList()
        }
        afterEach { clearMocks(userRepository, taskRepository, contentRepository, sharedPreferences, answers = false) }

        "isPersonalBoard" should {
            "return true if the owner is the user" {
                val viewModel = makeViewModel()
                viewModel.ownerID.value = "user-1"
                viewModel.isPersonalBoard shouldBe true
            }

            "return false if the owner is a team plan" {
                val viewModel = makeViewModel()
                viewModel.ownerID.value = "team-1"
                viewModel.isPersonalBoard shouldBe false
            }
        }

        "team plans" should {
            "allow switching owners once team plans exist" {
                val viewModel = makeViewModel()
                teamPlans.value = listOf(teamPlan("team-1", "Team"))
                viewModel.canSwitchOwners.value shouldBe true
                viewModel.teamPlans.keys shouldBe setOf("team-1")
            }

            "use the team plan summary as owner title" {
                val viewModel = makeViewModel()
                teamPlans.value = listOf(teamPlan("team-1", "Team"))
                viewModel.ownerID.value = "team-1"
                viewModel.ownerTitle shouldBe "Team"
            }
        }

        "cycleOwnerIDs" should {
            "cycle through all owners and wrap around" {
                val viewModel = makeViewModel()
                teamPlans.value = listOf(teamPlan("team-1", "One"), teamPlan("team-2", "Two"))
                viewModel.ownerID.value = "user-1"
                viewModel.cycleOwnerIDs()
                viewModel.ownerID.value shouldBe "team-1"
                viewModel.cycleOwnerIDs()
                viewModel.ownerID.value shouldBe "team-2"
                viewModel.cycleOwnerIDs()
                viewModel.ownerID.value shouldBe "user-1"
            }

            "not change the owner if there are no team plans" {
                val viewModel = makeViewModel()
                viewModel.ownerID.value = "user-1"
                viewModel.cycleOwnerIDs()
                viewModel.ownerID.value shouldBe "user-1"
            }
        }

        "filter" should {
            "only return tasks containing all active tags" {
                val viewModel = makeViewModel()
                val tagged = task(TaskType.HABIT, tagIDs = listOf("a", "b"))
                val partial = task(TaskType.HABIT, tagIDs = listOf("a"))
                viewModel.tags = mutableListOf("a", "b")
                viewModel.filter(listOf(tagged, partial)) shouldBe listOf(tagged)
            }

            "return only due and uncompleted dailies for the active filter" {
                val viewModel = makeViewModel()
                val due = task(TaskType.DAILY, isDue = true)
                val notDue = task(TaskType.DAILY, isDue = false)
                val completed = task(TaskType.DAILY, completed = true, isDue = true)
                viewModel.setActiveFilter(TaskType.DAILY, Task.FILTER_ACTIVE)
                viewModel.filter(listOf(due, notDue, completed)) shouldBe listOf(due)
            }

            "return not due or completed dailies for the gray filter" {
                val viewModel = makeViewModel()
                val due = task(TaskType.DAILY, isDue = true)
                val notDue = task(TaskType.DAILY, isDue = false)
                val completed = task(TaskType.DAILY, completed = true, isDue = true)
                viewModel.setActiveFilter(TaskType.DAILY, Task.FILTER_GRAY)
                viewModel.filter(listOf(due, notDue, completed)) shouldBe listOf(notDue, completed)
            }

            "split habits into weak and strong by value" {
                val viewModel = makeViewModel()
                val weak = task(TaskType.HABIT, value = 0.5)
                val strong = task(TaskType.HABIT, value = 1.0)
                viewModel.setActiveFilter(TaskType.HABIT, Task.FILTER_WEAK)
                viewModel.filter(listOf(weak, strong)) shouldBe listOf(weak)
                viewModel.setActiveFilter(TaskType.HABIT, Task.FILTER_STRONG)
                viewModel.filter(listOf(weak, strong)) shouldBe listOf(strong)
            }

            "return only todos with a due date for the dated filter" {
                val viewModel = makeViewModel()
                val dated = task(TaskType.TODO).apply { dueDate = java.util.Date() }
                val undated = task(TaskType.TODO)
                viewModel.setActiveFilter(TaskType.TODO, Task.FILTER_DATED)
                viewModel.filter(listOf(dated, undated)) shouldBe listOf(dated)
            }

            "return only completed todos for the completed filter" {
                val viewModel = makeViewModel()
                val completed = task(TaskType.TODO, completed = true)
                val open = task(TaskType.TODO)
                viewModel.setActiveFilter(TaskType.TODO, Task.FILTER_COMPLETED)
                viewModel.filter(listOf(completed, open)) shouldBe listOf(completed)
            }

            "return all tasks for the all filter" {
                val viewModel = makeViewModel()
                val tasks = listOf(task(TaskType.HABIT, value = 0.5), task(TaskType.HABIT, value = 2.0))
                viewModel.setActiveFilter(TaskType.HABIT, Task.FILTER_ALL)
                viewModel.filter(tasks) shouldBe tasks
            }

            "return an empty list unchanged" {
                makeViewModel().filter(emptyList()) shouldBe emptyList()
            }
        }

        "filterCount" should {
            "count active tags and a non default filter" {
                val viewModel = makeViewModel()
                viewModel.addActiveTag("a")
                viewModel.addActiveTag("b")
                viewModel.setActiveFilter(TaskType.HABIT, Task.FILTER_WEAK)
                viewModel.filterCount(TaskType.HABIT) shouldBe 3
                viewModel.isFiltering(TaskType.HABIT) shouldBe true
            }

            "not count the default filters" {
                val viewModel = makeViewModel()
                viewModel.setActiveFilter(TaskType.TODO, Task.FILTER_ACTIVE)
                viewModel.setActiveFilter(TaskType.HABIT, Task.FILTER_ALL)
                viewModel.filterCount(TaskType.TODO) shouldBe 0
                viewModel.filterCount(TaskType.HABIT) shouldBe 0
                viewModel.isFiltering(TaskType.HABIT) shouldBe false
            }
        }

        "active tags" should {
            "not add the same tag twice and update all filter sets" {
                val viewModel = makeViewModel()
                viewModel.addActiveTag("a")
                viewModel.addActiveTag("a")
                viewModel.tags shouldBe listOf("a")
                viewModel.getFilterSet(TaskType.DAILY)?.value?.third shouldBe listOf("a")
            }

            "remove tags" {
                val viewModel = makeViewModel()
                viewModel.addActiveTag("a")
                viewModel.removeActiveTag("a")
                viewModel.tags shouldBe emptyList()
                viewModel.getFilterSet(TaskType.TODO)?.value?.third shouldBe emptyList()
            }
        }

        "searchQuery" should {
            "update all filter sets" {
                val viewModel = makeViewModel()
                viewModel.searchQuery = "read"
                viewModel.getFilterSet(TaskType.HABIT)?.value?.first shouldBe "read"
                viewModel.getFilterSet(TaskType.TODO)?.value?.first shouldBe "read"
            }
        }

        "setActiveFilter" should {
            "store the filter and update the filter set" {
                val viewModel = makeViewModel()
                viewModel.setActiveFilter(TaskType.HABIT, Task.FILTER_WEAK)
                viewModel.getActiveFilter(TaskType.HABIT) shouldBe Task.FILTER_WEAK
                viewModel.getFilterSet(TaskType.HABIT)?.value?.second shouldBe Task.FILTER_WEAK
                verify { sharedPreferences.edit() }
            }

            "retrieve completed todos for the completed filter" {
                makeViewModel().setActiveFilter(TaskType.TODO, Task.FILTER_COMPLETED)
                coVerify(exactly = 1) { taskRepository.retrieveCompletedTodos() }
            }

            "update the daily default view if it changed" {
                makeViewModel().setActiveFilter(TaskType.DAILY, Task.FILTER_ACTIVE)
                coVerify(exactly = 1) { userRepository.updateUser("preferences.dailyDueDefaultView", true) }
            }
        }

        "getActiveFilter" should {
            "return null if no filter was set" {
                makeViewModel().getActiveFilter(TaskType.HABIT) shouldBe null
            }
        }

        "getTaskFilterPreference" should {
            "return the stored preference" {
                every { sharedPreferences.getString("filter_todo", Task.FILTER_ALL) } returns Task.FILTER_DATED
                makeViewModel().getTaskFilterPreference(TaskType.TODO) shouldBe Task.FILTER_DATED
            }
        }

        "refreshData" should {
            "retrieve the user and world state for the personal board" {
                val viewModel = makeViewModel()
                viewModel.ownerID.value = "user-1"
                var completed = false
                viewModel.refreshData { completed = true }
                coVerify(exactly = 1) { userRepository.retrieveUser(true, true) }
                coVerify(exactly = 1) { contentRepository.retrieveWorldState() }
                completed shouldBe true
            }

            "retrieve the team plan for a team board" {
                val viewModel = makeViewModel()
                viewModel.ownerID.value = "team-1"
                viewModel.refreshData { }
                coVerify(exactly = 1) { userRepository.retrieveTeamPlan("team-1") }
                coVerify(exactly = 0) { userRepository.retrieveUser(any(), any()) }
            }
        }

        "canScoreTask" should {
            "allow personal tasks" {
                makeViewModel().canScoreTask(task(TaskType.HABIT)) shouldBe true
            }

            "allow group tasks assigned to the user or to nobody" {
                val viewModel = makeViewModel()
                val assigned = task(TaskType.TODO).apply { group = TaskGroupPlan().apply { groupID = "team-1"; assignedUsers = RealmList("user-1") } }
                val unassigned = task(TaskType.TODO).apply { group = TaskGroupPlan().apply { groupID = "team-1" } }
                viewModel.canScoreTask(assigned) shouldBe true
                viewModel.canScoreTask(unassigned) shouldBe true
            }

            "not allow group tasks assigned to someone else" {
                val other = task(TaskType.TODO).apply { group = TaskGroupPlan().apply { groupID = "team-1"; assignedUsers = RealmList("user-2") } }
                makeViewModel().canScoreTask(other) shouldBe false
            }
        }

        "canEditTask" should {
            "allow personal tasks" {
                makeViewModel().canEditTask(task(TaskType.HABIT)) shouldBe true
            }

            "depend on the team plan privileges for group tasks" {
                val groupTask = task(TaskType.TODO).apply { group = TaskGroupPlan().apply { groupID = "team-1" } }
                every { userRepository.getTeamPlan("team-1") } returns flowOf(Group().apply { leaderID = "user-1" })
                makeViewModel().canEditTask(groupTask) shouldBe true
                every { userRepository.getTeamPlan("team-1") } returns flowOf(Group().apply { leaderID = "user-2" })
                makeViewModel().canEditTask(groupTask) shouldBe false
            }
        }

        "canAddTasks" should {
            "allow adding to the personal board" {
                val viewModel = makeViewModel()
                viewModel.ownerID.value = "user-1"
                viewModel.canAddTasks() shouldBe true
            }

            "only allow managers to add to a team board" {
                val viewModel = makeViewModel()
                viewModel.ownerID.value = "team-1"
                every { userRepository.getTeamPlan("team-1") } returns flowOf(Group().apply { managers = RealmList("user-1") })
                viewModel.canAddTasks() shouldBe true
                every { userRepository.getTeamPlan("team-1") } returns flowOf(null)
                viewModel.canAddTasks() shouldBe false
            }
        }
    })
