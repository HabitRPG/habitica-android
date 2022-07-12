package com.habitrpg.wearos.habitica.ui.activities

import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ActivityRyaBinding
import com.habitrpg.android.habitica.databinding.RowDailyBinding
import com.habitrpg.common.habitica.helpers.DeviceCommunication
import com.habitrpg.wearos.habitica.models.tasks.Task
import com.habitrpg.wearos.habitica.ui.viewHolders.tasks.DailyViewHolder
import com.habitrpg.wearos.habitica.ui.viewmodels.RYAViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

@AndroidEntryPoint
class RYAActivity : BaseActivity<ActivityRyaBinding, RYAViewModel>() {
    override val viewModel: RYAViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRyaBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        viewModel.tasks.observe(this) {
            if (it.isEmpty()) {
                runCron()
                return@observe
            }
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
            openRemoteActivity(DeviceCommunication.SHOW_RYA)
        }

        binding.startDayButton.setOnClickListener {
            runCron()
        }
    }

    lateinit var startTime: Date

    private fun runCron() {
        startTime = Date()
        startAnimatingProgress()
        binding.startDayButton.isEnabled = false
        binding.startingTextView.isVisible = true
        binding.startDayButton.isVisible = false
        binding.taskView.isVisible = false
        binding.descriptionView.isVisible = false
        binding.titleView.isVisible = false
        binding.phoneDescriptionView.isVisible = false
        binding.ryaButton.isVisible = false
        binding.phoneButton.isVisible = false
        viewModel.runCron {
            if (it) {
                lifecycleScope.launch {
                    val elapsed = Date().time - startTime.time
                    if (elapsed <= 1000) {
                        // always show it at least 1 second
                        delay(1000 - elapsed)
                    }
                    stopAnimatingProgress()
                    finish()
                }
            } else {
                stopAnimatingProgress()
                binding.startDayButton.isEnabled = true
                binding.startingTextView.isVisible = false
                binding.startDayButton.isVisible = true
                binding.taskView.isVisible = true
                binding.descriptionView.isVisible = true
                binding.titleView.isVisible = true
                binding.phoneDescriptionView.isVisible = true
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
            holder.onTaskScore = { viewModel.tappedTask(task) }
            val layoutParams = taskBinding.chip.layoutParams as LinearLayout.LayoutParams
            layoutParams.marginStart = 0
            layoutParams.marginEnd = 0
            taskBinding.chip.layoutParams = layoutParams
            holder.bind(task)
        }
    }
}