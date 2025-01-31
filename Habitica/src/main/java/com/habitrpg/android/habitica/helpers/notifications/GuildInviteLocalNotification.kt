package com.habitrpg.android.habitica.helpers.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.receivers.LocalNotificationActionReceiver

class GuildInviteLocalNotification(context: Context, identifier: String?) :
    HabiticaLocalNotification(context, identifier) {
    override fun configureMainIntent(intent: Intent) {
        super.configureMainIntent(intent)
        intent.putExtra("groupID", data?.get("groupID"))
    }

    override fun setNotificationActions(
        notificationId: Int,
        data: Map<String, String>
    ) {
        super.setNotificationActions(notificationId, data)
        val res = context.resources

        val acceptInviteIntent = Intent(context, LocalNotificationActionReceiver::class.java)
        acceptInviteIntent.action = res.getString(R.string.accept_guild_invite)
        val groupID = data["groupID"]
        acceptInviteIntent.putExtra("groupID", groupID)
        acceptInviteIntent.putExtra("NOTIFICATION_ID", notificationId)
        val pendingIntentAccept =
            PendingIntent.getBroadcast(
                context,
                groupID.hashCode(),
                acceptInviteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
            )
        notificationBuilder.addAction(0, "Accept", pendingIntentAccept)

        val rejectInviteIntent = Intent(context, LocalNotificationActionReceiver::class.java)
        rejectInviteIntent.action = res.getString(R.string.reject_guild_invite)
        rejectInviteIntent.putExtra("groupID", groupID)
        acceptInviteIntent.putExtra("NOTIFICATION_ID", notificationId)
        val pendingIntentReject =
            PendingIntent.getBroadcast(
                context,
                groupID.hashCode() + 1,
                rejectInviteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
            )
        notificationBuilder.addAction(0, "Reject", pendingIntentReject)
    }
}
