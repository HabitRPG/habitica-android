package com.habitrpg.android.habitica.helpers.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.receivers.LocalNotificationActionReceiver

/**
 * Created by keithholliday on 7/1/16.
 */
class QuestInviteLocalNotification(context: Context, identifier: String?) :
    HabiticaLocalNotification(context, identifier) {
    override fun getNotificationID(data: MutableMap<String, String>): Int {
        return 1000
    }

    override fun setNotificationActions(
        notificationId: Int,
        data: Map<String, String>
    ) {
        super.setNotificationActions(notificationId, data)
        val res = context.resources

        val acceptInviteIntent = Intent(context, LocalNotificationActionReceiver::class.java)
        acceptInviteIntent.action = res.getString(R.string.accept_quest_invite)
        acceptInviteIntent.putExtra("NOTIFICATION_ID", notificationId)
        val flags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        val pendingIntentAccept =
            PendingIntent.getBroadcast(
                context,
                3001,
                acceptInviteIntent,
                flags
            )
        notificationBuilder.addAction(0, "Accept", pendingIntentAccept)

        val rejectInviteIntent = Intent(context, LocalNotificationActionReceiver::class.java)
        rejectInviteIntent.action = res.getString(R.string.reject_quest_invite)
        rejectInviteIntent.putExtra("NOTIFICATION_ID", notificationId)
        val pendingIntentReject =
            PendingIntent.getBroadcast(
                context,
                2001,
                rejectInviteIntent,
                flags
            )
        notificationBuilder.addAction(0, "Reject", pendingIntentReject)
    }
}
