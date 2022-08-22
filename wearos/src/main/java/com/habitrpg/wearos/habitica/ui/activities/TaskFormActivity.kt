package com.habitrpg.wearos.habitica.ui.activities

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ActivityTaskFormBinding
import com.habitrpg.shared.habitica.models.tasks.TaskType
import com.habitrpg.wearos.habitica.ui.viewmodels.TaskFormViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaskFormActivity : BaseActivity<ActivityTaskFormBinding, TaskFormViewModel>() {
    var taskType: TaskType? = null
        set(value) {
            field = value
            updateTaskTypeButton(binding.todoButton, TaskType.TODO)
            updateTaskTypeButton(binding.dailyButton, TaskType.DAILY)
            updateTaskTypeButton(binding.habitButton, TaskType.HABIT)
            val typeName = getString(when(value) {
                TaskType.HABIT -> R.string.habit
                TaskType.DAILY -> R.string.daily
                TaskType.TODO -> R.string.todo
                TaskType.REWARD -> R.string.reward
                else -> R.string.task
            })
            binding.confirmationTitle.text = getString(R.string.new_task_x, typeName)
            binding.saveButton.setChipText(getString(R.string.save_task_x, typeName))
        }
    override val viewModel: TaskFormViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTaskFormBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        binding.editText.setOnClickListener {
            it.clearFocus()
            requestInput()
        }

        binding.editButton.setOnClickListener {
            binding.editTaskWrapper.isVisible = true
            binding.taskConfirmationWrapper.isVisible = false
            if (intent.extras?.containsKey("task_type") == true) {
                requestInput()
            }
        }
        binding.todoButton.setOnClickListener { taskType = TaskType.TODO }
        binding.dailyButton.setOnClickListener { taskType = TaskType.DAILY }
        binding.habitButton.setOnClickListener { taskType = TaskType.HABIT }

        binding.saveButton.setOnClickListener {
            binding.saveButton.isEnabled = false
            lifecycleScope.launch(CoroutineExceptionHandler { _, _ ->
                binding.saveButton.isEnabled = true
                binding.editTaskWrapper.isVisible = true
                binding.taskConfirmationWrapper.isVisible = false
            }) {
                viewModel.saveTask(binding.editText.text, taskType)
                val data = Intent()
                data.putExtra("task_type", taskType?.value)
                setResult(Activity.RESULT_OK, data)
                finish()

                parent.startActivity(Intent(parent, TaskListActivity::class.java).apply {
                    putExtra("task_type", taskType?.value)
                })
            }
        }
        if (intent.extras?.containsKey("task_type") == true) {
            taskType = TaskType.from(intent.getStringExtra("task_type"))
            binding.taskTypeHeader.isVisible = false
            binding.taskTypeWrapper.isVisible = false
            binding.header.textView.text = getString(R.string.create_task, taskType?.value)
            requestInput()
        } else {
            taskType = TaskType.TODO
            binding.header.textView.text = getString(R.string.new_task)
        }
    }

    private val inputResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val input = it.data?.getStringExtra("input")
        if (input?.isNotBlank() == true) {
            binding.editTaskWrapper.isVisible = false
            binding.taskConfirmationWrapper.isVisible = true
            binding.confirmationText.text = input
            binding.editText.setText(input)
        }
    }

    private fun requestInput() {
        val intent = Intent(this, InputActivity::class.java).apply {
            putExtra("title", getString(R.string.task_title_hint))
            putExtra("input", binding.editText.text.toString())
        }
        inputResult.launch(intent)
    }

    private fun updateTaskTypeButton(button: TextView, thisType: TaskType) {
        if (taskType == thisType) {
            button.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.watch_purple_10))
            button.background.alpha = 100
            button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_checked, 0)
        } else {
            button.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.watch_purple_5))
            button.background.alpha = 255
            button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_unchecked, 0)
        }
    }
}