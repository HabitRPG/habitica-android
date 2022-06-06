package com.habitrpg.wearos.habitica.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.wear.widget.WearableLinearLayoutManager
import com.habitrpg.common.habitica.models.tasks.TaskType
import com.habitrpg.wearos.habitica.R
import com.habitrpg.wearos.habitica.databinding.ActivityTasklistBinding
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
        binding.root.apply {
            layoutManager =
                WearableLinearLayoutManager(this@TaskListActivity, HabiticaScrollingLayoutCallback())
            adapter = this@TaskListActivity.adapter
        }

        viewModel.tasks.observe(this) {
            adapter.data = it
        }
    }

    private fun configureAdapter() {
        when (viewModel.taskType) {
            TaskType.HABIT -> {
                adapter = HabitListAdapter()
                adapter.title = getString(R.string.habits)
            }
            TaskType.DAILY -> {
                adapter = DailyListAdapter()
                adapter.title = getString(R.string.dailies)
            }
            TaskType.TODO -> {
                adapter = ToDoListAdapter()
                adapter.title = getString(R.string.todos)
            }
            TaskType.REWARD -> {
                adapter = RewardListAdapter()
                adapter.title = getString(R.string.rewards)
            }
            else -> {}
        }
    }
}