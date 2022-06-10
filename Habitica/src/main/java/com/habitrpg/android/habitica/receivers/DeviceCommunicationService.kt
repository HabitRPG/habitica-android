package com.habitrpg.android.habitica.receivers

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.common.habitica.api.HostConfig
import javax.inject.Inject

class DeviceCommunicationService: WearableListenerService() {
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
        }
    }

    private fun processAuthRequest(event: MessageEvent) {
        Log.d("DeviceCommunicationServ", "processAuthRequest: AUTH REQUESTED")
        messageClient.sendMessage(event.sourceNodeId, "/auth", "${hostConfig.userID}:${hostConfig.apiKey}".toByteArray())
    }
}