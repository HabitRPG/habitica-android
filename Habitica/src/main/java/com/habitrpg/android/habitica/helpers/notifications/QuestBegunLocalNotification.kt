package com.habitrpg.android.habitica.helpers.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

import com.habitrpg.android.habitica.ui.activities.MainActivity

/**
 * Created by keithholliday on 7/1/16.
 */
class QuestBegunLocalNotification(context: Context, identifier: String) : HabiticaLocalNotification(context, identifier) {
    override fun getNotificationID(): Int = 1000
}
