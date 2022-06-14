package com.habitrpg.wearos.habitica.ui.activities

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ActivityTaskFormBinding
import com.habitrpg.common.habitica.models.tasks.TaskType
import com.habitrpg.wearos.habitica.ui.viewmodels.TaskFormViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaskFormActivity: BaseActivity<ActivityTaskFormBinding, TaskFormViewModel>() {
    var taskType: TaskType? = null
    set(value) {
        field = value
        binding.taskTypeButton.text = value?.value
    }
    override val viewModel: TaskFormViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTaskFormBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)


        binding.editText.doOnTextChanged { text, _, _, _ ->
            binding.saveButton.isEnabled = text?.isNotBlank() == true
        }
        binding.taskTypeButton.setOnClickListener {
            showTaskTypeSelector()
        }
        binding.saveButton.setOnClickListener {
            binding.saveButton.isEnabled = false
            lifecycleScope.launch {
                viewModel.saveTask(binding.editText.text, taskType)
                finish()
            }
        }
        if (intent.extras?.containsKey("task_type") == true) {
            taskType = TaskType.from(intent.getStringExtra("task_type"))
            binding.taskTypeButton.isVisible = false
            binding.header.textView.text = getString(R.string.create_task, taskType?.value)
        } else {
            taskType = TaskType.TODO
            binding.header.textView.text = getString(R.string.create_task_title)
        }
    }

    private fun showTaskTypeSelector() {
        val taskTypes = arrayOf(
            TaskType.HABIT,
            TaskType.DAILY,
            TaskType.TODO,
            TaskType.REWARD
        )
        val adapter = ArrayAdapter(this, R.layout.spinner_item, taskTypes)
        val alert = AlertDialog.Builder(this).setAdapter(adapter) { _, which ->
            taskType = taskTypes[which]
            binding.taskTypeButton.text = taskType?.value
        }
        alert.show()
    }
}