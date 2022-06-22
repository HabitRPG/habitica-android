package com.habitrpg.wearos.habitica.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import com.habitrpg.android.habitica.databinding.ActivityTaskDetailBinding
import com.habitrpg.wearos.habitica.ui.viewmodels.TaskDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class TaskDetailActivity: BaseActivity<ActivityTaskDetailBinding, TaskDetailViewModel>() {
    val messageClient: MessageClient by lazy { Wearable.getMessageClient(this) }
    val capabilityClient: CapabilityClient by lazy { Wearable.getCapabilityClient(this) }

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
        lifecycleScope.launch(Dispatchers.IO) {
            val info = Tasks.await(capabilityClient.getCapability("edit_task", CapabilityClient.FILTER_REACHABLE))
            val nodeID = info.nodes.firstOrNull { it.isNearby }
            if (nodeID != null) {
                Tasks.await(messageClient.sendMessage(nodeID.id, "/tasks/edit", viewModel.taskID?.toByteArray()))
            }
        }
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