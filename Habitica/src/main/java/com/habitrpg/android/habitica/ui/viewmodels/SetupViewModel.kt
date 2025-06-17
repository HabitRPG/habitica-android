package com.habitrpg.android.habitica.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SetupCustomizationRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.models.SetupCustomization
import com.habitrpg.android.habitica.models.tasks.Days
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.shared.habitica.models.tasks.Frequency
import com.habitrpg.shared.habitica.models.tasks.TaskType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(val userRepository: UserRepository, val taskRepository: TaskRepository, val inventoryRepository: InventoryRepository) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _selectedTaskCategories = MutableStateFlow<Set<String>>(emptySet())
    val selectedTaskCategories: StateFlow<Set<String>> = _selectedTaskCategories

    fun initializeUser(initialUser: User?) {
        _user.value = initialUser
    }

    fun equipCustomization(item: SetupCustomization) {
        val currentUser = _user.value ?: return
        when (item.category) {
            SetupCustomizationRepository.CATEGORY_BODY -> currentUser.preferences?.shirt = item.key
            SetupCustomizationRepository.CATEGORY_HAIR -> when (item.subcategory) {
                SetupCustomizationRepository.SUBCATEGORY_COLOR -> currentUser.preferences?.hair?.color = item.key
                SetupCustomizationRepository.SUBCATEGORY_BANGS -> currentUser.preferences?.hair?.bangs = item.key.toIntOrNull() ?: 0
                SetupCustomizationRepository.SUBCATEGORY_PONYTAIL -> currentUser.preferences?.hair?.base = item.key.toIntOrNull() ?: 0
                else -> {}
            }
            SetupCustomizationRepository.CATEGORY_SKIN -> currentUser.preferences?.skin = item.key
            SetupCustomizationRepository.CATEGORY_EXTRAS -> when (item.subcategory) {
                SetupCustomizationRepository.SUBCATEGORY_WHEELCHAIR -> currentUser.preferences?.chair = item.key
                SetupCustomizationRepository.SUBCATEGORY_FLOWER -> currentUser.preferences?.hair?.flower = item.key.toIntOrNull() ?: 0
                SetupCustomizationRepository.SUBCATEGORY_GLASSES -> currentUser.items?.gear?.equipped?.eyeWear = item.key
                else -> {}
            }
            else -> {}
        }
        _user.value = currentUser
    }

    fun getActiveCustomization(category: String, subcategory: String): String {
        val currentUser = _user.value
        return when (category) {
            SetupCustomizationRepository.CATEGORY_BODY -> currentUser?.preferences?.shirt
            SetupCustomizationRepository.CATEGORY_HAIR -> when (subcategory) {
                SetupCustomizationRepository.SUBCATEGORY_COLOR -> currentUser?.preferences?.hair?.color
                SetupCustomizationRepository.SUBCATEGORY_BANGS -> currentUser?.preferences?.hair?.bangs?.toString()
                SetupCustomizationRepository.SUBCATEGORY_PONYTAIL -> currentUser?.preferences?.hair?.base?.toString()
                else -> ""
            }
            SetupCustomizationRepository.CATEGORY_SKIN -> currentUser?.preferences?.skin
            SetupCustomizationRepository.CATEGORY_EXTRAS -> when (subcategory) {
                SetupCustomizationRepository.SUBCATEGORY_WHEELCHAIR -> currentUser?.preferences?.chair
                SetupCustomizationRepository.SUBCATEGORY_FLOWER -> currentUser?.preferences?.hair?.flower?.toString()
                SetupCustomizationRepository.SUBCATEGORY_GLASSES -> currentUser?.items?.gear?.equipped?.eyeWear
                else -> ""
            }
            else -> ""
        } ?: ""
    }

    fun selectTaskCategory(category: String) {
        val currentCategories = _selectedTaskCategories.value.toMutableSet()
        if (currentCategories.contains(category)) {
            currentCategories.remove(category)
        } else {
            currentCategories.add(category)
        }
        _selectedTaskCategories.value = currentCategories
    }

    suspend fun saveSetup(context: Context) {
        userRepository.updateUser(mapOf(
            "preferences.shirt" to _user.value?.preferences?.shirt,
            "preferences.hair.color" to _user.value?.preferences?.hair?.color,
            "preferences.hair.bangs" to _user.value?.preferences?.hair?.bangs,
            "preferences.hair.base" to _user.value?.preferences?.hair?.base,
            "preferences.hair.flower" to _user.value?.preferences?.hair?.flower,
            "preferences.skin" to _user.value?.preferences?.skin,
            "preferences.chair" to _user.value?.preferences?.chair,
        ))
        if (_user.value?.items?.gear?.equipped?.eyeWear?.isNotBlank() == true) {
            inventoryRepository.equipGear(_user.value?.items?.gear?.equipped?.eyeWear ?: "", false)
        }
        val tasks = createSampleTasks(context)
        taskRepository.createTasks(tasks)
        userRepository.retrieveUser(true, forced = true)
    }

    fun createSampleTasks(context: Context): List<Task> {
        val taskTexts = listOf(
            listOf(TYPE_WORK, TaskType.HABIT, context.getString(R.string.setup_task_work_1), true, false),
            listOf(TYPE_WORK, TaskType.DAILY, context.getString(R.string.setup_task_work_2)),
            listOf(TYPE_WORK, TaskType.TODO, context.getString(R.string.setup_task_work_3)),
            listOf(
                TYPE_EXERCISE,
                TaskType.HABIT,
                context.getString(R.string.setup_task_exercise_1),
                true,
                false
            ),
            listOf(TYPE_EXERCISE, TaskType.DAILY, context.getString(R.string.setup_task_exercise_2)),
            listOf(TYPE_EXERCISE, TaskType.TODO, context.getString(R.string.setup_task_exercise_3)),
            listOf(
                TYPE_HEALTH,
                TaskType.HABIT,
                context.getString(R.string.setup_task_healthWellness_1),
                true,
                true
            ),
            listOf(TYPE_HEALTH, TaskType.DAILY, context.getString(R.string.setup_task_healthWellness_2)),
            listOf(TYPE_HEALTH, TaskType.TODO, context.getString(R.string.setup_task_healthWellness_3)),
            listOf(
                TYPE_SCHOOL,
                TaskType.HABIT,
                context.getString(R.string.setup_task_school_1),
                true,
                true
            ),
            listOf(TYPE_SCHOOL, TaskType.DAILY, context.getString(R.string.setup_task_school_2)),
            listOf(TYPE_SCHOOL, TaskType.TODO, context.getString(R.string.setup_task_school_3)),
            listOf(TYPE_TEAMS, TaskType.HABIT, context.getString(R.string.setup_task_teams_1), true, false),
            listOf(TYPE_TEAMS, TaskType.DAILY, context.getString(R.string.setup_task_teams_2)),
            listOf(TYPE_TEAMS, TaskType.TODO, context.getString(R.string.setup_task_teams_3)),
            listOf(
                TYPE_CHORES,
                TaskType.HABIT,
                context.getString(R.string.setup_task_chores_1),
                true,
                false
            ),
            listOf(TYPE_CHORES, TaskType.DAILY, context.getString(R.string.setup_task_chores_2)),
            listOf(TYPE_CHORES, TaskType.TODO, context.getString(R.string.setup_task_chores_3)),
            listOf(
                TYPE_CREATIVITY,
                TaskType.HABIT,
                context.getString(R.string.setup_task_creativity_1),
                true,
                false
            ),
            listOf(TYPE_CREATIVITY, TaskType.DAILY, context.getString(R.string.setup_task_creativity_2)),
            listOf(TYPE_CREATIVITY, TaskType.TODO, context.getString(R.string.setup_task_creativity_3))
        )
        val tasks = ArrayList<Task>()
        for (task in taskTexts) {
            val taskGroup = task[0] as? String
            if (selectedTaskCategories.value.contains(taskGroup)) {
                val taskObject: Task =
                    if (task.size == 5) {
                        this.makeTaskObject(
                            task[1] as? TaskType,
                            task[2] as? String,
                            task[3] as? Boolean,
                            task[4] as? Boolean
                        )
                    } else {
                        this.makeTaskObject(task[1] as? TaskType, task[2] as? String, null, null)
                    }
                tasks.add(taskObject)
            }
        }
        tasks.add(
            makeTaskObject(
                TaskType.HABIT,
                context.getString(R.string.setup_task_habit_1),
                up = true,
                down = false,
                notes = context.getString(R.string.setup_task_habit_1_notes)
            )
        )
        tasks.add(
            makeTaskObject(
                TaskType.HABIT,
                context.getString(R.string.setup_task_habit_2),
                up = false,
                down = true,
                notes = context.getString(R.string.setup_task_habit_2_notes)
            )
        )
        tasks.add(
            makeTaskObject(
                TaskType.REWARD,
                context.getString(R.string.setup_task_reward),
                null,
                null,
                context.getString(R.string.setup_task_reward_notes)
            )
        )
        tasks.add(
            makeTaskObject(
                TaskType.TODO,
                context.getString(R.string.setup_task_join_habitica),
                null,
                null,
                context.getString(R.string.setup_task_join_habitica_notes)
            )
        )
        return tasks
    }

    private fun makeTaskObject(
        type: TaskType?,
        text: String?,
        up: Boolean?,
        down: Boolean?,
        notes: String? = null
    ): Task {
        val task = Task()
        task.id = UUID.randomUUID().toString()
        task.text = text ?: ""
        task.notes = notes
        task.priority = 1.0f
        task.type = type ?: TaskType.HABIT
        task.frequency = Frequency.DAILY

        if (type == TaskType.HABIT) {
            task.up = up
            task.down = down
        }

        if (type == TaskType.DAILY) {
            task.frequency = Frequency.WEEKLY
            task.startDate = Date()
            task.everyX = 1
            val days = Days()
            days.m = true
            days.t = true
            days.w = true
            days.th = true
            days.f = true
            days.s = true
            days.su = true
            task.repeat = days
        }

        return task
    }

    companion object {
        const val TYPE_EXERCISE = "exercise"
        const val TYPE_HEALTH = "healthWellness"
        const val TYPE_WORK = "work"
        const val TYPE_SCHOOL = "school"
        const val TYPE_TEAMS = "teams"
        const val TYPE_CHORES = "chores"
        const val TYPE_CREATIVITY = "creativity"
    }
}
