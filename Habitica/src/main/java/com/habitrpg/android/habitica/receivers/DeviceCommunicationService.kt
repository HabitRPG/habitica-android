package com.habitrpg.android.habitica.receivers

import android.content.Intent
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.ui.activities.TaskFormActivity
import com.habitrpg.common.habitica.api.HostConfig
import javax.inject.Inject

class DeviceCommunicationService : WearableListenerService() {
    @Inject
    lateinit var hostConfig: HostConfig

    private val messageClient by lazy { Wearable.getMessageClient(this) }

    init {
        HabiticaBaseApplication.userComponent?.inject(this)
    }

    override fun onMessageReceived(event: MessageEvent) {
        super.onMessageReceived(event)
        when (event.path) {
            "/request/auth" -> processAuthRequest(event)
            "/tasks/edit" -> openTaskForm(event)
        }
    }

    private fun openTaskForm(event: MessageEvent) {
        val taskID = String(event.data)
        val startIntent = Intent(this, TaskFormActivity::class.java).apply {
            putExtra(TaskFormActivity.TASK_ID_KEY, taskID)
        }
         startActivity(startIntent)
    }

    private fun processAuthRequest(event: MessageEvent) {
        messageClient.sendMessage(
            event.sourceNodeId,
            "/auth",
            "${hostConfig.userID}:${hostConfig.apiKey}".toByteArray()
        )
    }
}