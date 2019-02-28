package com.habitrpg.android.habitica.helpers.notifications

import android.content.Context
import android.content.Intent

class ChatMentionNotification(context: Context, identifier: String) : HabiticaLocalNotification(context, identifier) {

    override fun configureMainIntent(intent: Intent) {
        super.configureMainIntent(intent)
        intent.putExtra("type", data?.get("type"))
        intent.putExtra("groupID", data?.get("groupID"))
    }
}