package com.habitrpg.android.habitica.helpers.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.os.bundleOf
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.withMutableFlag
import com.habitrpg.android.habitica.receivers.LocalNotificationActionReceiver
import com.habitrpg.common.habitica.helpers.EmojiParser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GroupActivityNotification(context: Context, identifier: String?) :
    HabiticaLocalNotification(context, identifier) {
    override fun getNotificationID(data: MutableMap<String, String>): Int {
        return data["groupID"].hashCode()
    }

    override fun configureNotificationBuilder(data: MutableMap<String, String>): NotificationCompat.Builder {
        val user = Person.Builder().setName("You").build()
        val message = makeMessageFromData(data)
        var style =
            NotificationCompat.MessagingStyle(user)
                .setGroupConversation(true)
                .setConversationTitle(data["groupName"])

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val existingNotifications =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notificationManager?.activeNotifications?.filter { it.id == getNotificationID(data) }
            } else {
                null
            }
        val oldMessages =
            existingNotifications?.firstOrNull()?.notification?.extras?.getBundle("messages")
                ?.get("messages") as? ArrayList<Map<String, String>> ?: arrayListOf()
        for (oldMessage in oldMessages) {
            style = style.addMessage(makeMessageFromData(oldMessage))
        }
        style = style.addMessage(message)
        oldMessages.add(data)
        return super.configureNotificationBuilder(data)
            .setStyle(style)
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setExtras(bundleOf(Pair("messages", bundleOf(Pair("messages", oldMessages)))))
    }

    private fun makeMessageFromData(data: Map<String, String>): NotificationCompat.MessagingStyle.Message {
        val sender = Person.Builder().setName(data["senderName"]).build()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val timestamp = data["timestamp"]?.let { dateFormat.parse(it) } ?: Date()
        val messageText = EmojiParser.parseEmojis(data["message"]?.trim { it <= ' ' })
        return NotificationCompat.MessagingStyle.Message(
            messageText,
            timestamp.time,
            sender,
        )
    }

    override fun setNotificationActions(
        notificationId: Int,
        data: Map<String, String>,
    ) {
        super.setNotificationActions(notificationId, data)
        val groupID = data["groupID"] ?: return

        val actionName = context.getString(R.string.group_message_reply)
        val replyLabel: String = context.getString(R.string.reply)
        val remoteInput: RemoteInput =
            RemoteInput.Builder(actionName).run {
                setLabel(replyLabel)
                build()
            }
        val intent = Intent(context, LocalNotificationActionReceiver::class.java)
        intent.action = actionName
        intent.putExtra("groupID", groupID)
        intent.putExtra("NOTIFICATION_ID", notificationId)
        val replyPendingIntent: PendingIntent =
            PendingIntent.getBroadcast(
                context,
                groupID.hashCode(),
                intent,
                withMutableFlag(PendingIntent.FLAG_UPDATE_CURRENT),
            )

        val action: NotificationCompat.Action =
            NotificationCompat.Action.Builder(
                R.drawable.ic_send_grey_600_24dp,
                context.getString(R.string.reply),
                replyPendingIntent,
            )
                .addRemoteInput(remoteInput)
                .build()
        notificationBuilder.addAction(action)
    }

    override fun configureMainIntent(intent: Intent) {
        super.configureMainIntent(intent)
        intent.putExtra("type", data?.get("type"))
        intent.putExtra("groupID", data?.get("groupID"))
    }
}
