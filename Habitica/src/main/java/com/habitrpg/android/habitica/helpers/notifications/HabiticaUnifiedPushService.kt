package com.habitrpg.android.habitica.helpers.notifications

import android.util.Log
import com.google.firebase.messaging.RemoteMessage
import com.habitrpg.android.habitica.R
import dagger.hilt.android.AndroidEntryPoint
import java.nio.charset.Charset
import java.util.UUID
import javax.inject.Inject
import org.json.JSONException
import org.json.JSONObject
import org.unifiedpush.android.connector.FailedReason
import org.unifiedpush.android.connector.PushService
import org.unifiedpush.android.connector.data.PushEndpoint
import org.unifiedpush.android.connector.data.PushMessage

@AndroidEntryPoint
class HabiticaUnifiedPushService : PushService() {
    @Inject
    lateinit var pushNotificationManager: PushNotificationManager

    override fun onNewEndpoint(endpoint: PushEndpoint, instance: String) {
        pushNotificationManager.registerUnifiedPushEndpoint(endpoint.url)
        Log.d(TAG, "UnifiedPush endpoint updated for instance $instance")
    }

    override fun onMessage(message: PushMessage, instance: String) {
        val payload = buildDataMap(message)
        if (payload.isEmpty()) {
            Log.w(TAG, "UnifiedPush message for instance $instance contained no usable data")
            return
        }

        val title = payload["title"]
        val body = payload["body"] ?: payload["message"]
        val remoteMessageBuilder = RemoteMessage.Builder("${applicationContext.packageName}.unifiedpush")
            .setMessageId(UUID.randomUUID().toString())

        payload.forEach { (key, value) ->
            if (!value.isNullOrEmpty()) {
                remoteMessageBuilder.addData(key, value)
            }
        }

        val remoteMessage = remoteMessageBuilder.build()
        PushNotificationManager.displayNotification(remoteMessage, applicationContext, pushNotificationManager)
        Log.d(TAG, "UnifiedPush message delivered for instance $instance (title=${title.orEmpty()}, identifier=${payload["identifier"].orEmpty()})")
    }

    override fun onRegistrationFailed(reason: FailedReason, instance: String) {
        pushNotificationManager.unregisterUnifiedPushEndpoint()
        Log.w(TAG, "UnifiedPush registration failed for instance $instance: $reason")
    }

    override fun onUnregistered(instance: String) {
        pushNotificationManager.unregisterUnifiedPushEndpoint()
        Log.d(TAG, "UnifiedPush unregistered for instance $instance")
    }

    companion object {
        private const val TAG = "HabiticaUnifiedPush"
    }

    private fun buildDataMap(pushMessage: PushMessage): MutableMap<String, String> {
        val data = mutableMapOf<String, String>()
        val raw = try {
            String(pushMessage.content, Charset.forName("UTF-8"))
        } catch (e: Exception) {
            Log.w(TAG, "Unable to decode UnifiedPush payload", e)
            return data
        }

        if (raw.isBlank()) {
            return data
        }

        try {
            val json = JSONObject(raw)
            json.optString("identifier")?.takeIf { it.isNotBlank() }?.let { data["identifier"] = it }
            json.optString("title")?.takeIf { it.isNotBlank() }?.let { data["title"] = it }
            json.optString("body")?.takeIf { it.isNotBlank() }?.let { data["body"] = it }
            json.optString("message")?.takeIf { it.isNotBlank() }?.let {
                data["message"] = it
                if (!data.containsKey("body")) {
                    data["body"] = it
                }
            }
            json.optString("priority")?.takeIf { it.isNotBlank() }?.let { data["priority"] = it }

            val payload = json.optJSONObject("payload")
            payload?.let {
                val keys = it.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = it.opt(key)
                    if (value != null && value.toString().isNotEmpty()) {
                        data[key] = value.toString()
                    }
                }
            }
        } catch (jsonError: JSONException) {
            // Treat the raw message as body text when JSON parsing fails
            data["body"] = raw
            data["message"] = raw
            Log.w(TAG, "UnifiedPush payload not JSON, using raw message", jsonError)
        }

        if (!data.containsKey("title")) {
            data["title"] = applicationContext.getString(R.string.app_name)
        }

        return data
    }
}
