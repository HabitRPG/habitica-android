package com.habitrpg.android.habitica.helpers.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.receivers.LocalNotificationActionReceiver

/**
 * Created by keithholliday on 6/28/16.
 */
class PartyInviteLocalNotification(context: Context, identifier: String) : HabiticaLocalNotification(context, identifier) {

    override fun setNotificationActions(data: Map<String, String>) {
        super.setNotificationActions(data)
        val res = context.resources

        val acceptInviteIntent = Intent(context, LocalNotificationActionReceiver::class.java)
        acceptInviteIntent.action = res.getString(R.string.accept_party_invite)
        acceptInviteIntent.putExtra("groupID", this.data?.get("groupID"))
        val pendingIntentAccept = PendingIntent.getBroadcast(
                context,
                3000,
                acceptInviteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        notificationBuilder.addAction(0, context.getString(R.string.accept), pendingIntentAccept)

        val rejectInviteIntent = Intent(context, LocalNotificationActionReceiver::class.java)
        rejectInviteIntent.action = res.getString(R.string.reject_party_invite)
        rejectInviteIntent.putExtra("groupID", this.data?.get("groupID"))
        val pendingIntentReject = PendingIntent.getBroadcast(
                context,
                2000,
                rejectInviteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        notificationBuilder.addAction(0, context.getString(R.string.reject), pendingIntentReject)
    }
}
