package com.habitrpg.android.habitica.ui.viewmodels

import android.content.SharedPreferences
import android.content.res.Resources
import android.text.format.DateUtils
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.TagRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.AmplitudeManager
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.GroupPlanInfoProvider
import com.habitrpg.android.habitica.models.TeamPlan
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.shared.habitica.models.responses.TaskDirection
import com.habitrpg.shared.habitica.models.responses.TaskScoringResult
import com.habitrpg.shared.habitica.models.tasks.TaskType
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.Case
import io.realm.OrderedRealmCollection
import io.realm.RealmQuery
import io.realm.Sort
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    userRepository : UserRepository,
    userViewModel : MainUserViewModel,
    val taskRepository : TaskRepository,
    val tagRepository : TagRepository,
    val appConfigManager : AppConfigManager,
    val sharedPreferences : SharedPreferences
) : BaseViewModel(userRepository, userViewModel), GroupPlanInfoProvider {
    private var owners : List<Pair<String, CharSequence>> = listOf()
    var canSwitchOwners = MutableLiveData<Boolean?>()
    val ownerID : MutableLiveData<String?> by lazy {
        MutableLiveData()
    }
    var teamPlans = mapOf<String, TeamPlan>()
    var initialPreferenceFilterSet : Boolean = false

    val isPersonalBoard : Boolean
        get() {
            return ownerID.value == userViewModel.userID
        }
    val ownerTitle : CharSequence
        get() {
            return owners.firstOrNull { it.first == ownerID.value }?.second ?: ""
        }

    init {
        viewModelScope.launch(ExceptionHandler.coroutine()) {
            userRepository.getTeamPlans()
                .collect { plans ->
                    teamPlans = plans.associateBy { it.id }
                    owners =
                        listOf(Pair(userViewModel.userID, userViewModel.displayName)) + plans.map {
                            Pair(
                                it.id,
                                it.summary
                            )
                        }
                    if (owners.size > 1 && canSwitchOwners.value != false) {
                        canSwitchOwners.value = owners.size > 1
                    }
                }
        }
    }

    internal fun refreshData(onComplete : () -> Unit) {
        viewModelScope.launch(ExceptionHandler.coroutine()) {
            if (isPersonalBoard) {
                userRepository.retrieveUser(
                    withTasks = true,
                    forced = true
                )
                if (activeFilters[TaskType.TODO] == Task.FILTER_COMPLETED) {
                    taskRepository.retrieveCompletedTodos()
                }
            } else {
                userRepository.retrieveTeamPlan(ownerID.value ?: "")
            }
            onComplete()
        }
    }

    fun cycleOwnerIDs() {
        if (owners.size <= 1) return
        val nextIndex = owners.indexOfFirst { it.first == ownerID.value } + 1
        if (nextIndex < owners.size) {
            ownerID.value = owners[nextIndex].first
        } else {
            ownerID.value = owners[0].first
        }
    }

    fun scoreTask(
        task : Task,
        direction : TaskDirection,
        onResult : (TaskScoringResult, Int) -> Unit
    ) {
        viewModelScope.launch(ExceptionHandler.coroutine()) {
            taskRepository.taskChecked(
                null,
                task.id ?: "",
                direction == TaskDirection.UP,
                false
            ) { result ->
                onResult(result, task.value.toInt())
                if (!DateUtils.isToday(sharedPreferences.getLong("last_task_reporting", 0))) {
                    AmplitudeManager.sendEvent(
                        "task scored",
                        AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR,
                        AmplitudeManager.EVENT_HITTYPE_EVENT
                    )
                    sharedPreferences.edit {
                        putLong("last_task_reporting", Date().time)
                    }
                }
            }
        }
    }

    private val filterSets : HashMap<TaskType, MutableLiveData<Triple<String?, String?, List<String>>>> =
        hashMapOf(
            Pair(TaskType.HABIT, MutableLiveData()),
            Pair(TaskType.DAILY, MutableLiveData()),
            Pair(TaskType.TODO, MutableLiveData())
        )

    fun getFilterSet(type : TaskType) : MutableLiveData<Triple<String?, String?, List<String>>>? {
        return filterSets[type]
    }

    var searchQuery : String? = null
        set(value) {
            field = value
            filterSets.forEach {
                val old = it.value.value
                it.value.value = Triple(value, old?.second, old?.third ?: listOf())
            }
        }
    private val activeFilters = HashMap<TaskType, String>()

    var tags : MutableList<String> = mutableListOf()
        set(value) {
            field = value
            filterSets.forEach {
                val old = it.value.value
                it.value.value = Triple(old?.first, old?.second, field)
            }
        }

    fun addActiveTag(tagID : String) {
        if (!tags.contains(tagID)) {
            tags.add(tagID)
        }
        filterSets.forEach {
            val old = it.value.value
            it.value.value = Triple(old?.first, old?.second, tags)
        }
    }

    fun removeActiveTag(tagID : String) {
        if (tags.contains(tagID)) {
            tags.remove(tagID)
        }
        filterSets.forEach {
            val old = it.value.value
            it.value.value = Triple(old?.first, old?.second, tags)
        }
    }

    fun filterCount(type : TaskType?) : Int {
        return this.tags.size + if (isTaskFilterActive(type)) 1 else 0
    }

    fun isFiltering(type : TaskType?) : Boolean {
        return filterCount(type) > 0
    }

    private fun isTaskFilterActive(type : TaskType?) : Boolean {
        if (activeFilters[type] == null) {
            return false
        }
        return if (TaskType.TODO == type) {
            Task.FILTER_ACTIVE != activeFilters[type]
        } else {
            Task.FILTER_ALL != activeFilters[type]
        }
    }

    fun filter(tasks : List<Task>) : List<Task> {
        if (tasks.isEmpty()) {
            return tasks
        }
        val filtered = ArrayList<Task>()
        var activeFilter : String? = null
        if (activeFilters.size > 0) {
            activeFilter = activeFilters[tasks[0].type]
        }
        for (task in tasks) {
            if (isFiltered(task, activeFilter)) {
                filtered.add(task)
            }
        }

        return filtered
    }

    private fun isFiltered(task : Task, activeFilter : String?) : Boolean {
        if (!task.containsAllTagIds(tags)) {
            return false
        }
        return if (activeFilter != null && activeFilter != Task.FILTER_ALL) {
            when (activeFilter) {
                Task.FILTER_ACTIVE -> if (task.type == TaskType.DAILY) {
                    task.isDisplayedActive
                } else {
                    !task.completed
                }

                Task.FILTER_GRAY -> task.completed || !task.isDisplayedActive
                Task.FILTER_WEAK -> task.value < 1
                Task.FILTER_STRONG -> task.value >= 1
                Task.FILTER_DATED -> task.dueDate != null
                Task.FILTER_COMPLETED -> task.completed
                else -> true
            }
        } else {
            true
        }
    }

    fun setActiveFilter(type : TaskType, activeFilter : String) {
        activeFilters[type] = activeFilter
        filterSets[type]?.value = Triple(searchQuery, activeFilter, tags)

        if (activeFilters[TaskType.TODO] == Task.FILTER_COMPLETED) {
            viewModelScope.launchCatching {
                taskRepository.retrieveCompletedTodos()
            }
        }
    }

    fun getActiveFilter(type : TaskType?) : String? {
        return if (activeFilters.containsKey(type)) {
            activeFilters[type]
        } else {
            null
        }
    }

    fun createQuery(unfilteredData : OrderedRealmCollection<Task>) : RealmQuery<Task>? {
        if (!unfilteredData.isValid) {
            return null
        }
        var query = unfilteredData.where()

        if (unfilteredData.size != 0) {
            val taskType = unfilteredData[0].type
            val activeFilter = getActiveFilter(taskType)

            if (tags.size > 0) {
                query = query.`in`("tags.id", tags.toTypedArray())
            }
            if (searchQuery?.isNotEmpty() == true) {
                query = query
                    .beginGroup()
                    .contains("text", searchQuery ?: "", Case.INSENSITIVE)
                    .or()
                    .contains("notes", searchQuery ?: "", Case.INSENSITIVE)
                    .endGroup()
            }
            if (activeFilter != null && activeFilter != Task.FILTER_ALL) {
                when (activeFilter) {
                    Task.FILTER_ACTIVE -> query = if (TaskType.DAILY == taskType) {
                        query.equalTo("completed", false).equalTo("isDue", true)
                    } else {
                        query.equalTo("completed", false)
                    }

                    Task.FILTER_GRAY ->
                        query =
                            query.equalTo("completed", true).or().equalTo("isDue", false)

                    Task.FILTER_WEAK -> query = query.lessThan("value", 1.0)
                    Task.FILTER_STRONG -> query = query.greaterThanOrEqualTo("value", 1.0)
                    Task.FILTER_DATED ->
                        query =
                            query.isNotNull("dueDate").equalTo("completed", false).sort("dueDate")

                    Task.FILTER_COMPLETED -> query = query.equalTo("completed", true)
                }
            }
            if (activeFilter != Task.FILTER_DATED) {
                query = query.sort("position", Sort.ASCENDING, "dateCreated", Sort.DESCENDING)
            }
        }
        return query
    }

    override fun canScoreTask(task : Task) : Boolean {
        if (!task.isGroupTask) {
            return true
        }
        return task.isAssignedToUser(userViewModel.userID) || task.group?.assignedUsers?.isEmpty() != false
    }

    override suspend fun canEditTask(task : Task) : Boolean {
        if (!task.isGroupTask) {
            return true
        }
        val groupID = task.group?.groupID ?: return true
        val group = userRepository.getTeamPlan(groupID).firstOrNull()
        return group?.hasTaskEditPrivileges(userViewModel.userID) ?: false
    }

    override suspend fun canAddTasks() : Boolean {
        if (isPersonalBoard) {
            return true
        }
        val groupID = ownerID.value ?: return true
        val group = userRepository.getTeamPlan(groupID).firstOrNull()
        return group?.hasTaskEditPrivileges(userViewModel.userID) ?: false
    }

    override fun assignedTextForTask(resources : Resources, assignedUsers : List<String>) : String {
        return if (assignedUsers.contains(userViewModel.userID)) {
            if (assignedUsers.size == 1) {
                resources.getString(R.string.you)
            } else {
                resources.getQuantityString(
                    R.plurals.you_x_others,
                    assignedUsers.size - 1,
                    assignedUsers.size - 1
                )
            }
        } else {
            if (assignedUsers.size == 1) {
                userViewModel.currentTeamPlanMembers.value?.firstOrNull { it.id == assignedUsers.first() }?.displayName
                    ?: ""
            } else {
                resources.getQuantityString(
                    R.plurals.people,
                    assignedUsers.size,
                    assignedUsers.size
                )
            }
        }
    }
}
