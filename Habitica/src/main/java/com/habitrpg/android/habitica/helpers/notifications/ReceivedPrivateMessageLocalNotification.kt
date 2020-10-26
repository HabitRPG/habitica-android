package com.habitrpg.android.habitica.helpers.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import androidx.core.os.bundleOf
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.receivers.LocalNotificationActionReceiver
import com.habitrpg.android.habitica.ui.helpers.EmojiParser


class ReceivedPrivateMessageLocalNotification(context: Context, identifier: String) : HabiticaLocalNotification(context, identifier) {
    override fun configureNotificationBuilder(data: MutableMap<String, String>): NotificationCompat.Builder {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val existingNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager?.activeNotifications?.filter { it.id == getNotificationID(data) }
        } else null
        val messageText = EmojiParser.parseEmojis(data["message"]?.trim { it <= ' ' })
        val oldMessages = existingNotifications?.firstOrNull()?.notification?.extras?.getStringArrayList("messages") ?: arrayListOf()
        var style = NotificationCompat.InboxStyle()
        for (oldMessage in oldMessages) {
            style = style.addLine(oldMessage)
        }
        style = style.addLine(messageText)
        oldMessages.add(messageText)
        var notification = super.configureNotificationBuilder(data)
                .setExtras(bundleOf(Pair("messages", oldMessages)))
        if (oldMessages.size > 1) {
            val notificationTitle = if (data["senderName"] != null) {
                context.getString(R.string.inbox_messages_title, oldMessages.size, data["senderName"])
            } else {
                context.getString(R.string.inbox_messages_title_nosender, oldMessages.size)
            }
            notification = notification
                    .setContentTitle(notificationTitle)
                    .setStyle(style)
            title = null
        }
        return notification
    }

    override fun getNotificationID(data: MutableMap<String, String>): Int {
        return data["senderName"].hashCode()
    }

    override fun setNotificationActions(data: Map<String, String>) {
        super.setNotificationActions(data)
        val senderID = data["replyTo"] ?: return

        val actionName = context.getString(R.string.inbox_message_reply)
        val replyLabel: String = context.getString(R.string.reply)
        val remoteInput: RemoteInput = RemoteInput.Builder(actionName).run {
            setLabel(replyLabel)
            build()
        }
        val intent = Intent(context, LocalNotificationActionReceiver::class.java)
        intent.action = actionName
        intent.putExtra("senderID", senderID)
        val replyPendingIntent: PendingIntent =
                PendingIntent.getBroadcast(context, senderID.hashCode(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT)

        val action: NotificationCompat.Action =
                NotificationCompat.Action.Builder(R.drawable.ic_send_grey_600_24dp,
                        context.getString(R.string.reply), replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build()
        notificationBuilder.addAction(action)
    }
}
