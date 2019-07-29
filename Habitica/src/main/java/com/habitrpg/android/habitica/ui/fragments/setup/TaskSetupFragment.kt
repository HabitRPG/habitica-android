package com.habitrpg.android.habitica.ui.fragments.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.tasks.Days
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.SpeechBubbleView
import com.habitrpg.android.habitica.ui.activities.SetupActivity
import com.habitrpg.android.habitica.ui.adapter.setup.TaskSetupAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.bindOptionalView
import com.habitrpg.android.habitica.ui.helpers.resetViews
import java.util.*

class TaskSetupFragment : BaseFragment() {


    var activity: SetupActivity? = null
    var width: Int = 0
    private val recyclerView: RecyclerView? by bindOptionalView(R.id.recyclerView)
    private val avatarView: AvatarView? by bindOptionalView(R.id.avatarView)
    private val speechBubbleView: SpeechBubbleView? by bindOptionalView(R.id.speech_bubble)
    internal var adapter: TaskSetupAdapter = TaskSetupAdapter()
    private var taskGroups: List<List<String>> = listOf()
    private var tasks: List<List<Any>> = listOf()
    private var user: User? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        return container?.inflate(R.layout.fragment_setup_tasks)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetViews()

        this.setTasks()

        this.adapter = TaskSetupAdapter()
        this.adapter.setTaskList(this.taskGroups)
        this.recyclerView?.layoutManager = GridLayoutManager(activity, 2)
        this.recyclerView?.adapter = this.adapter

        if (this.user != null) {
            this.updateAvatar()
        }

    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && context != null) {
            speechBubbleView?.animateText(context?.getString(R.string.task_setup_description) ?: "")
        }
    }

    fun setUser(user: User?) {
        this.user = user
        if (avatarView != null) {
            updateAvatar()
        }
    }

    private fun updateAvatar() {
        user?.let {
            avatarView?.setAvatar(it)
        }
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private fun setTasks() {
        this.taskGroups = listOf(listOf(getString(R.string.setup_group_work), "work"), listOf(getString(R.string.setup_group_exercise), "exercise"), listOf(getString(R.string.setup_group_health), "healthWellness"), listOf(getString(R.string.setup_group_school), "school"), listOf(getString(R.string.setup_group_teams), "teams"), listOf(getString(R.string.setup_group_chores), "chores"), listOf(getString(R.string.setup_group_creativity), "creativity"), listOf(getString(R.string.setuP_group_other), "other"))

        this.tasks = listOf(listOf("work", Task.TYPE_HABIT, getString(R.string.setup_task_work_1), true, false), listOf("work", Task.TYPE_DAILY, getString(R.string.setup_task_work_2)), listOf("work", Task.TYPE_TODO, getString(R.string.setup_task_work_3)),
                listOf("exercise", Task.TYPE_HABIT, getString(R.string.setup_task_exercise_1), true, false), listOf("exercise", Task.TYPE_DAILY, getString(R.string.setup_task_exercise_2)), listOf("exercise", Task.TYPE_TODO, getString(R.string.setup_task_exercise_3)),
                listOf("healthWellness", Task.TYPE_HABIT, getString(R.string.setup_task_healthWellness_1), true, true), listOf("healthWellness", Task.TYPE_DAILY, getString(R.string.setup_task_healthWellness_2)), listOf("healthWellness", Task.TYPE_TODO, getString(R.string.setup_task_healthWellness_3)),
                listOf("school", Task.TYPE_HABIT, getString(R.string.setup_task_school_1), true, true), listOf("school", Task.TYPE_DAILY, getString(R.string.setup_task_school_2)), listOf("school", Task.TYPE_TODO, getString(R.string.setup_task_school_3)),
                listOf("teams", Task.TYPE_HABIT, getString(R.string.setup_task_teams_1), true, false), listOf("teams", Task.TYPE_DAILY, getString(R.string.setup_task_teams_2)), listOf("teams", Task.TYPE_TODO, getString(R.string.setup_task_teams_3)),
                listOf("chores", Task.TYPE_HABIT, getString(R.string.setup_task_chores_1), true, false), listOf("chores", Task.TYPE_DAILY, getString(R.string.setup_task_chores_2)), listOf("chores", Task.TYPE_TODO, getString(R.string.setup_task_chores_3)),
                listOf("creativity", Task.TYPE_HABIT, getString(R.string.setup_task_creativity_1), true, false), listOf("creativity", Task.TYPE_DAILY, getString(R.string.setup_task_creativity_2)), listOf("creativity", Task.TYPE_TODO, getString(R.string.setup_task_creativity_3)))
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
        tasks.add(makeTaskObject(Task.TYPE_HABIT, getString(R.string.setup_task_habit_1), true, false, getString(R.string.setup_task_habit_1_notes)))
        tasks.add(makeTaskObject(Task.TYPE_HABIT, getString(R.string.setup_task_habit_2), false, true, getString(R.string.setup_task_habit_2_notes)))
        tasks.add(makeTaskObject(Task.TYPE_REWARD, getString(R.string.setup_task_reward), null, null, getString(R.string.setup_task_reward_notes)))
        tasks.add(makeTaskObject(Task.TYPE_TODO, getString(R.string.setup_task_join_habitica), null, null, getString(R.string.setup_task_join_habitica_notes)))
        return tasks
    }

    private fun makeTaskObject(type: String?, text: String?, up: Boolean?, down: Boolean?, notes: String? = null): Task {
        val task = Task()
        task.text = text ?: ""
        task.notes = notes
        task.priority = 1.0f
        task.type = type ?: ""

        if (type == Task.TYPE_HABIT) {
            task.up = up
            task.down = down
        }

        if (type == Task.TYPE_DAILY) {
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

}
