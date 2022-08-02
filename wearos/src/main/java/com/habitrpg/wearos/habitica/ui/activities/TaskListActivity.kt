package com.habitrpg.wearos.habitica.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.wear.widget.WearableLinearLayoutManager
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ActivityTasklistBinding
import com.habitrpg.android.habitica.databinding.EmptyTaskListBinding
import com.habitrpg.common.habitica.models.responses.TaskDirection
import com.habitrpg.common.habitica.models.responses.TaskScoringResult
import com.habitrpg.common.habitica.models.tasks.TaskType
import com.habitrpg.wearos.habitica.models.tasks.Task
import com.habitrpg.wearos.habitica.ui.adapters.DailyListAdapter
import com.habitrpg.wearos.habitica.ui.adapters.HabitListAdapter
import com.habitrpg.wearos.habitica.ui.adapters.RewardListAdapter
import com.habitrpg.wearos.habitica.ui.adapters.TaskListAdapter
import com.habitrpg.wearos.habitica.ui.adapters.ToDoListAdapter
import com.habitrpg.wearos.habitica.ui.viewmodels.TaskListViewModel
import com.habitrpg.wearos.habitica.util.HabiticaScrollingLayoutCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaskListActivity : BaseActivity<ActivityTasklistBinding, TaskListViewModel>() {
    private lateinit var adapter: TaskListAdapter
    override val viewModel: TaskListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTasklistBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        configureAdapter()

        binding.recyclerView.apply {
            overScrollMode = View.OVER_SCROLL_ALWAYS
            layoutManager =
                WearableLinearLayoutManager(
                    this@TaskListActivity,
                    HabiticaScrollingLayoutCallback()
                )
            adapter = this@TaskListActivity.adapter
            emptyViewBuilder = {
                val emptyBinding = EmptyTaskListBinding.inflate(layoutInflater)
                emptyBinding.disconnected.root.isVisible = !appStateManager.isAppConnected.value
                emptyBinding.header.textView.text = getTitle(viewModel.taskCount.value)
                emptyBinding.descriptionView.text = getString(
                    R.string.no_tasks, getString(
                        when (viewModel.taskType) {
                            TaskType.HABIT -> R.string.habit
                            TaskType.DAILY -> R.string.daily
                            TaskType.TODO -> R.string.todo
                            TaskType.REWARD -> R.string.reward
                            else -> R.string.task
                        }
                    )
                )
                emptyBinding.root
            }
            onRefresh = {
                viewModel.retrieveFullUserData()
            }
        }

        viewModel.tasks.observe(this) {
            if (!it.isNullOrEmpty()) {
                adapter.data = it
            }
        }
        viewModel.taskCount.observe(this) {
            adapter.title = getTitle(it)
        }
        lifecycleScope.launch {
            appStateManager.isAppConnected.collect {
                adapter.isDisconnected = !it
            }
        }


        adapter.onTaskScore = {
            scoreTask(it)
        }
        adapter.onTaskTapped = {
            openTaskDetailActivity(it)
        }

        binding.addTaskButton.setOnClickListener { openTaskFormActivity() }
    }

    private fun openTaskDetailActivity(task: Task) {
        startActivity(Intent(this, TaskDetailActivity::class.java).apply {
            putExtra("task_id", task.id)
        })
    }

    private var taskToScore: Task? = null
    private val habitDirectionIntentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val direction = result.data?.getStringExtra("direction")
                    ?.let { TaskDirection.valueOf(it) }
                    ?: TaskDirection.UP

                taskToScore?.let { task ->
                    viewModel.scoreTask(task, direction) {
                        if (it != null) {
                            showTaskScoringResult(it)
                        }
                    }
                }
            }
        }

    private fun scoreTask(task: Task) {
        var direction = TaskDirection.UP
        if (task.type == TaskType.HABIT) {
            if (task.up == true && task.down == true) {
                taskToScore = task
                habitDirectionIntentLauncher.launch(
                    Intent(
                        this,
                        HabitDirectionActivity::class.java
                    ).apply {
                        putExtra("task_id", task.id)
                    })
                return
            } else if (task.up != true && task.down != true) {
                return
            } else {
                direction = if (task.up == true) TaskDirection.UP else TaskDirection.DOWN
            }
        } else if (task.completed) {
            direction = TaskDirection.DOWN
        }
        viewModel.scoreTask(task, direction) {
            if (it != null) {
                showTaskScoringResult(it)
            }
        }
    }

    private fun showTaskScoringResult(result: TaskScoringResult) {
        TaskResultActivity.show(this, result)
    }

    private fun openTaskFormActivity() {
        startActivity(Intent(this, TaskFormActivity::class.java).apply {
            putExtra("task_type", viewModel.taskType?.value)
        })
        overridePendingTransition(R.anim.scale_up, 0)
    }

    private fun configureAdapter() {
        when (viewModel.taskType) {
            TaskType.HABIT -> {
                adapter = HabitListAdapter()
            }
            TaskType.DAILY -> {
                adapter = DailyListAdapter()
            }
            TaskType.TODO -> {
                adapter = ToDoListAdapter()
            }
            TaskType.REWARD -> {
                adapter = RewardListAdapter()
            }
            else -> {}
        }
        adapter.title = getTitle(null)
        adapter.onRefresh = {
            viewModel.retrieveFullUserData()
        }
    }

    private fun getTitle(count: Int?): String {
        val taskType = viewModel.taskType ?: return ""
        return if (count != null) {
            when (taskType) {
                TaskType.HABIT -> resources.getQuantityString(R.plurals.x_habits, count, count)
                TaskType.DAILY -> resources.getQuantityString(R.plurals.x_dailies, count, count)
                TaskType.TODO -> resources.getQuantityString(R.plurals.x_todos, count, count)
                TaskType.REWARD -> resources.getQuantityString(R.plurals.x_rewards, count, count)
            }
        } else {
            when (taskType) {
                TaskType.HABIT -> getString(R.string.habits)
                TaskType.DAILY -> getString(R.string.dailies)
                TaskType.TODO -> getString(R.string.todos)
                TaskType.REWARD -> getString(R.string.rewards)
            }
        }
    }
}