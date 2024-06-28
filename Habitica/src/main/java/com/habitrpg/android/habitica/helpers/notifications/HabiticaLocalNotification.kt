package com.habitrpg.android.habitica.helpers.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.annotation.CallSuper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.withImmutableFlag
import com.habitrpg.android.habitica.ui.activities.MainActivity
import java.util.Date

/**
 * Created by keithholliday on 6/28/16.
 */
abstract class HabiticaLocalNotification(
    protected var context: Context,
    protected var identifier: String?
) {
    protected var data: Map<String, String>? = null
    protected var title: String? = null
    protected var message: String? = null

    protected var notificationBuilder =
        NotificationCompat.Builder(context, "default")
            .setSmallIcon(R.drawable.ic_gryphon_white)
            .setAutoCancel(true)

    open fun configureNotificationBuilder(data: MutableMap<String, String>): NotificationCompat.Builder {
        val path = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        return notificationBuilder
            .setSound(path)
            .setColor(ContextCompat.getColor(context, R.color.brand_300))
    }

    @CallSuper
    open fun notifyLocally(
        title: String?,
        message: String?,
        data: MutableMap<String, String>
    ) {
        this.title = title
        this.message = message

        var notificationBuilder = configureNotificationBuilder(data)

        if (this.title != null) {
            notificationBuilder = notificationBuilder.setContentTitle(title)
        }
        if (this.message != null) {
            notificationBuilder = notificationBuilder.setContentText(message)
        }

        if (this.title == null && this.message == null) {
            return
        }

        val notificationId = getNotificationID(data)
        this.setNotificationActions(notificationId, data)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    fun setExtras(data: Map<String, String>) {
        this.data = data
    }

    protected open fun setNotificationActions(
        notificationId: Int,
        data: Map<String, String>
    ) {
        val intent = Intent(context, MainActivity::class.java)
        configureMainIntent(intent)
        intent.putExtra("NOTIFICATION_ID", notificationId)
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                3000,
                intent,
                withImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT)
            )
        notificationBuilder.setContentIntent(pendingIntent)
    }

    protected open fun configureMainIntent(intent: Intent) {
        intent.putExtra("notificationIdentifier", identifier)
        intent.putExtra("notificationTimeStamp", Date().time)
        if (data?.containsKey("openURL") == true) {
            intent.putExtra("openURL", data?.get("openURL"))
        }
    }

    protected open fun getNotificationID(data: MutableMap<String, String>): Int {
        return Date().time.toInt()
    }
}
