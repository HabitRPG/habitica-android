package com.habitrpg.wearos.habitica.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.children
import androidx.core.view.isVisible
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ActivityRyaBinding
import com.habitrpg.android.habitica.databinding.RowDailyBinding
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.wearos.habitica.models.tasks.Task
import com.habitrpg.wearos.habitica.ui.viewHolders.tasks.DailyViewHolder
import com.habitrpg.wearos.habitica.ui.viewmodels.RYAViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RYAActivity : BaseActivity<ActivityRyaBinding, RYAViewModel>() {
    override val viewModel: RYAViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRyaBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        viewModel.tasks.observe(this) {
            createTaskListViews(it)
        }

        binding.ryaButton.setOnClickListener {
            binding.titleView.text = getString(R.string.check_off_yesterday)
            binding.descriptionView.isVisible = false
            binding.ryaButton.isVisible = false
            binding.phoneButton.isVisible = false
            binding.taskView.isVisible = true
            binding.startDayButton.isVisible = true
        }

        binding.phoneButton.setOnClickListener {
            openRemoteActivity("/show/rya")
        }

        binding.startDayButton.setOnClickListener {
            startAnimatingProgress()
            binding.startDayButton.isEnabled = false
            viewModel.runCron {
                stopAnimatingProgress()
                if (it) {
                    finish()
                } else {
                    binding.startDayButton.isEnabled = true
                }
            }
        }
    }

    private fun createTaskListViews(list: List<Task>) {
        binding.taskView.removeAllViews()
        for (task in list) {
            val taskBinding = RowDailyBinding.inflate(layoutInflater, binding.taskView, true)
            val holder = DailyViewHolder(taskBinding.root)
            taskBinding.root.setOnClickListener {
                viewModel.tappedTask(task)
            }
            val verticalPadding = 2.dpToPx(this)
            taskBinding.root.children.first().setPadding(0, verticalPadding, 0, verticalPadding)
            holder.bind(task)
        }
    }
}