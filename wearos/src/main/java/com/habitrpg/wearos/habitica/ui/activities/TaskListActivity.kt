package com.habitrpg.wearos.habitica.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.wear.widget.WearableLinearLayoutManager
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ActivityTasklistBinding
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

@AndroidEntryPoint
class TaskListActivity: BaseActivity<ActivityTasklistBinding, TaskListViewModel>() {
    private lateinit var adapter: TaskListAdapter
    override val viewModel: TaskListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTasklistBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        configureAdapter()
        binding.recyclerView.apply {
            layoutManager =
                WearableLinearLayoutManager(this@TaskListActivity, HabiticaScrollingLayoutCallback())
            adapter = this@TaskListActivity.adapter
        }

        viewModel.tasks.observe(this) {
            adapter.data = it
            adapter.title = getTitle(it.size)
        }

        adapter.onTaskScore = {
            scoreTask(it)
        }

        viewModel.user.observe(this) {

        }

        binding.addTaskButton.setOnClickListener { openTaskFormActivity() }
    }

    private fun scoreTask(task: Task) {
        var direction = TaskDirection.UP
        if (task.type == TaskType.HABIT) {
            if (task.up == true && task.down == true) {
                startActivity(Intent(this, HabitDirectionActivity::class.java).apply {
                    putExtra("task_id", task.id)
                })
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
    }

    private fun getTitle(count: Int?): String {
        val taskType = viewModel.taskType ?: return ""
        return if (count != null) {
            when (taskType) {
                TaskType.HABIT -> getString(R.string.x_habits, count)
                TaskType.DAILY -> getString(R.string.x_dailies, count)
                TaskType.TODO -> getString(R.string.x_todos, count)
                TaskType.REWARD -> getString(R.string.x_rewards, count)
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