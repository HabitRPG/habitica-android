package com.habitrpg.wearos.habitica.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
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

        viewModel.tasks.observe(
            this,
            object : Observer<List<Task>> {
                override fun onChanged(value: List<Task>) {
                    if (value.isEmpty()) {
                        runCron()
                        viewModel.tasks.removeObserver(this)
                    } else {
                        binding.scrollView.isVisible = true
                        createTaskListViews(value)
                        viewModel.tasks.removeObserver(this)
                    }
                }
            }
        )

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

    override fun onDestroy() {
        if (!viewModel.hasRunCron) {
            startActivity(
                Intent(this, RYAActivity::class.java)
                    .apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
            )
        }
        super.onDestroy()
    }

    private lateinit var startTime: Date

    private fun runCron() {
        startTime = Date()
        startAnimatingProgress()
        binding.startDayButton.isEnabled = false
        binding.startingTextView.isVisible = true
        binding.scrollView.isVisible = false
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
                binding.scrollView.isVisible = true
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
