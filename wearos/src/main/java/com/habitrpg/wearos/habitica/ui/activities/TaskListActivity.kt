package com.habitrpg.wearos.habitica.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.wear.widget.WearableLinearLayoutManager
import com.habitrpg.common.habitica.models.tasks.TaskType
import com.habitrpg.wearos.habitica.R
import com.habitrpg.wearos.habitica.databinding.ActivityTasklistBinding
import com.habitrpg.wearos.habitica.ui.adapters.TaskListAdapter
import com.habitrpg.wearos.habitica.ui.viewmodels.TaskListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskListActivity: BaseActivity<ActivityTasklistBinding>() {
    private val adapter = TaskListAdapter()
    private val viewModel: TaskListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTasklistBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        binding.root.apply {
            layoutManager =
                WearableLinearLayoutManager(this@TaskListActivity, HabiticaScrollingLayoutCallback())
            adapter = this@TaskListActivity.adapter
        }
        setAdapterTitle()

        viewModel.tasks.observe(this) {
            adapter.data = it
        }
    }

    private fun setAdapterTitle() {
        adapter.title = when (viewModel.taskType) {
            TaskType.HABIT -> getString(R.string.habits)
            TaskType.DAILY -> getString(R.string.dailies)
            TaskType.TODO -> getString(R.string.todos)
            TaskType.REWARD -> getString(R.string.rewards)
            null -> ""
        }
    }
}