package com.habitrpg.wearos.habitica.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.habitrpg.android.habitica.databinding.ActivityTaskDetailBinding
import com.habitrpg.wearos.habitica.ui.viewmodels.TaskDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class TaskDetailActivity: BaseActivity<ActivityTaskDetailBinding, TaskDetailViewModel>() {

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
        sendMessage("edit_task", "/tasks/edit", viewModel.taskID?.toByteArray())
    }

    private fun subscribeUI() {
        viewModel.task.observe(this) {
            binding.taskTypeView.text = it?.type?.value?.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
            binding.taskTextView.text = it?.text
            if (it?.notes?.isNotBlank() == true) {
                binding.taskNotesView.text = it.notes
                binding.taskNotesView.isVisible = true
            } else {
                binding.taskNotesView.isVisible = false
            }
        }
    }
}