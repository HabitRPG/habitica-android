package com.habitrpg.wearos.habitica.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.wear.widget.WearableLinearLayoutManager
import com.habitrpg.wearos.habitica.databinding.ActivityTasklistBinding
import com.habitrpg.wearos.habitica.models.tasks.Task
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
            isEdgeItemsCenteringEnabled = true
            layoutManager =
                WearableLinearLayoutManager(this@TaskListActivity, HabiticaScrollingLayoutCallback())
            adapter = this@TaskListActivity.adapter
        }

        adapter.data = listOf(
            Task().apply { text = "Test 1" },
            Task().apply { text = "Test 2" },
            Task().apply { text = "Test 3" },
            Task().apply { text = "Test 4" },
            Task().apply { text = "Test 5" }
        )

        viewModel.tasks.observe(this) {
            adapter.data = it
        }
    }
}