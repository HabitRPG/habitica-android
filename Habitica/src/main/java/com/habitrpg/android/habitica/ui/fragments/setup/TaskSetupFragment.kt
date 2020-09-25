package com.habitrpg.android.habitica.ui.fragments.setup

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.databinding.FragmentSetupTasksBinding
import com.habitrpg.android.habitica.models.tasks.Days
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.SetupActivity
import com.habitrpg.android.habitica.ui.adapter.setup.TaskSetupAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.shared.habitica.models.tasks.TaskType
import java.util.*

class TaskSetupFragment : BaseFragment<FragmentSetupTasksBinding>() {

    var activity: SetupActivity? = null
    var width: Int = 0

    override var binding: FragmentSetupTasksBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSetupTasksBinding {
        return FragmentSetupTasksBinding.inflate(inflater, container, false)
    }

    internal var adapter: TaskSetupAdapter = TaskSetupAdapter()
    private var taskGroups: List<List<String>> = listOf()
    private var tasks: List<List<Any>> = listOf()
    private var user: User? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.setTasks()

        this.adapter = TaskSetupAdapter()
        this.adapter.setTaskList(this.taskGroups)
        binding?.recyclerView?.layoutManager = GridLayoutManager(activity, 2)
        binding?.recyclerView?.adapter = this.adapter

        if (this.user != null) {
            this.updateAvatar()
        }

        binding?.heartIcon?.setImageDrawable(BitmapDrawable(HabiticaIconsHelper.imageOfHeartLightBg()))
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && context != null) {
            binding?.speechBubble?.animateText(context?.getString(R.string.task_setup_description) ?: "")
        }
    }

    fun setUser(user: User?) {
        this.user = user
        if (binding?.avatarView != null) {
            updateAvatar()
        }
    }

    private fun updateAvatar() {
        user?.let {
            binding?.avatarView?.setAvatar(it)
        }
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private fun setTasks() {
        this.taskGroups = listOf(listOf(getString(R.string.setup_group_work), TYPE_WORK), listOf(getString(R.string.setup_group_exercise), TYPE_EXERCISE), listOf(getString(R.string.setup_group_health), TYPE_HEALTH), listOf(getString(R.string.setup_group_school), TYPE_SCHOOL), listOf(getString(R.string.setup_group_teams), TYPE_TEAMS), listOf(getString(R.string.setup_group_chores), TYPE_CHORES), listOf(getString(R.string.setup_group_creativity), TYPE_CREATIVITY), listOf(getString(R.string.setuP_group_other), TYPE_OTHER))

        this.tasks = listOf(listOf(TYPE_WORK, TaskType.TYPE_HABIT, getString(R.string.setup_task_work_1), true, false), listOf(TYPE_WORK, TaskType.TYPE_DAILY, getString(R.string.setup_task_work_2)), listOf(TYPE_WORK, TaskType.TYPE_TODO, getString(R.string.setup_task_work_3)),
                listOf(TYPE_EXERCISE, TaskType.TYPE_HABIT, getString(R.string.setup_task_exercise_1), true, false), listOf(TYPE_EXERCISE, TaskType.TYPE_DAILY, getString(R.string.setup_task_exercise_2)), listOf(TYPE_EXERCISE, TaskType.TYPE_TODO, getString(R.string.setup_task_exercise_3)),
                listOf(TYPE_HEALTH, TaskType.TYPE_HABIT, getString(R.string.setup_task_healthWellness_1), true, true), listOf(TYPE_HEALTH, TaskType.TYPE_DAILY, getString(R.string.setup_task_healthWellness_2)), listOf(TYPE_HEALTH, TaskType.TYPE_TODO, getString(R.string.setup_task_healthWellness_3)),
                listOf(TYPE_SCHOOL, TaskType.TYPE_HABIT, getString(R.string.setup_task_school_1), true, true), listOf(TYPE_SCHOOL, TaskType.TYPE_DAILY, getString(R.string.setup_task_school_2)), listOf(TYPE_SCHOOL, TaskType.TYPE_TODO, getString(R.string.setup_task_school_3)),
                listOf(TYPE_TEAMS, TaskType.TYPE_HABIT, getString(R.string.setup_task_teams_1), true, false), listOf(TYPE_TEAMS, TaskType.TYPE_DAILY, getString(R.string.setup_task_teams_2)), listOf(TYPE_TEAMS, TaskType.TYPE_TODO, getString(R.string.setup_task_teams_3)),
                listOf(TYPE_CHORES, TaskType.TYPE_HABIT, getString(R.string.setup_task_chores_1), true, false), listOf(TYPE_CHORES, TaskType.TYPE_DAILY, getString(R.string.setup_task_chores_2)), listOf(TYPE_CHORES, TaskType.TYPE_TODO, getString(R.string.setup_task_chores_3)),
                listOf(TYPE_CREATIVITY, TaskType.TYPE_HABIT, getString(R.string.setup_task_creativity_1), true, false), listOf(TYPE_CREATIVITY, TaskType.TYPE_DAILY, getString(R.string.setup_task_creativity_2)), listOf(TYPE_CREATIVITY, TaskType.TYPE_TODO, getString(R.string.setup_task_creativity_3)))
    }

    fun createSampleTasks(): List<Task> {
        val groups = ArrayList<String>()
        for ((i, checked) in this.adapter.checkedList.withIndex()) {
            if (checked) {
                groups.add(this.taskGroups[i][1])
            }
        }
        val tasks = ArrayList<Task>()
        for (task in this.tasks) {
            val taskGroup = task[0] as? String
            if (groups.contains(taskGroup)) {
                val taskObject: Task = if (task.size == 5) {
                    this.makeTaskObject(task[1] as? String, task[2] as? String, task[3] as? Boolean, task[4] as? Boolean)
                } else {
                    this.makeTaskObject(task[1] as? String, task[2] as? String, null, null)
                }
                tasks.add(taskObject)
            }
        }
        tasks.add(makeTaskObject(TaskType.TYPE_HABIT, getString(R.string.setup_task_habit_1), true, false, getString(R.string.setup_task_habit_1_notes)))
        tasks.add(makeTaskObject(TaskType.TYPE_HABIT, getString(R.string.setup_task_habit_2), false, true, getString(R.string.setup_task_habit_2_notes)))
        tasks.add(makeTaskObject(TaskType.TYPE_REWARD, getString(R.string.setup_task_reward), null, null, getString(R.string.setup_task_reward_notes)))
        tasks.add(makeTaskObject(TaskType.TYPE_TODO, getString(R.string.setup_task_join_habitica), null, null, getString(R.string.setup_task_join_habitica_notes)))
        return tasks
    }

    private fun makeTaskObject(type: String?, text: String?, up: Boolean?, down: Boolean?, notes: String? = null): Task {
        val task = Task()
        task.text = text ?: ""
        task.notes = notes
        task.priority = 1.0f
        task.type = type ?: ""

        if (type == TaskType.TYPE_HABIT) {
            task.up = up
            task.down = down
        }

        if (type == TaskType.TYPE_DAILY) {
            task.frequency = "weekly"
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
        const val TYPE_OTHER = "other"
    }

}
