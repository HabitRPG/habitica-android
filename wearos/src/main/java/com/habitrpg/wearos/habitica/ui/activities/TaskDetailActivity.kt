package com.habitrpg.wearos.habitica.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ActivityTaskDetailBinding
import com.habitrpg.shared.habitica.models.tasks.TaskType
import com.habitrpg.shared.habitica.models.tasks.streakString
import com.habitrpg.wearos.habitica.ui.viewmodels.TaskDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat

@AndroidEntryPoint
class TaskDetailActivity : BaseActivity<ActivityTaskDetailBinding, TaskDetailViewModel>() {
    override val viewModel: TaskDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        binding.editButton.setOnClickListener {
            openEditFormOnPhone()
        }

        subscribeUI()
    }

    private fun openEditFormOnPhone() {
        sendMessage("edit_task", "/tasks/edit", viewModel.taskID?.toByteArray()) {}
        startActivity(
            Intent(this, ContinuePhoneActivity::class.java)
        )
    }

    private fun subscribeUI() {
        viewModel.task.observe(this) { task ->
            binding.taskTypeView.text =
                when (task?.type) {
                    TaskType.HABIT -> getString(R.string.habit)
                    TaskType.DAILY -> getString(R.string.daily)
                    TaskType.TODO -> getString(R.string.todo)
                    TaskType.REWARD -> getString(R.string.reward)
                    null -> ""
                }
            binding.taskTypeView.setTextColor(
                ContextCompat.getColor(
                    this,
                    task?.extraLightTaskColor ?: R.color.watch_white
                )
            )
            binding.taskTextView.text = task?.text
            if (task?.notes?.isNotBlank() == true) {
                binding.taskNotesView.text = task.notes
                binding.taskNotesView.isVisible = true
            } else {
                binding.taskNotesView.isVisible = false
            }
            if (task?.type == TaskType.TODO) {
                task.dueDate?.let { dueDate ->
                    val format = DateFormat.getDateInstance()
                    binding.taskStreakView.text = format.format(dueDate)
                    binding.taskStreakView.isVisible = true
                    binding.taskStreakView.setCompoundDrawables(null, null, null, null)
                } ?: run {
                    binding.taskStreakView.isVisible = false
                }
            } else {
                val streakString = task?.streakString
                if (streakString != null) {
                    binding.taskStreakView.isVisible = true
                    binding.taskStreakView.text = streakString
                } else {
                    binding.taskStreakView.isVisible = false
                }
            }
        }
    }
}
