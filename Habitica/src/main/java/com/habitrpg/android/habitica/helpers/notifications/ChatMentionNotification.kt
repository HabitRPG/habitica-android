package com.habitrpg.android.habitica.helpers.notifications

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class ChatMentionNotification(context: Context, identifier: String?) :
    HabiticaLocalNotification(context, identifier) {
    override fun configureNotificationBuilder(data: MutableMap<String, String>): NotificationCompat.Builder {
        val style =
            NotificationCompat.BigTextStyle()
                .setBigContentTitle(title)
                .bigText(message)
        return super.configureNotificationBuilder(data)
            .setStyle(style)
    }

    override fun configureMainIntent(intent: Intent) {
        super.configureMainIntent(intent)
        intent.putExtra("type", data?.get("type"))
        intent.putExtra("groupID", data?.get("groupID"))
    }
}
