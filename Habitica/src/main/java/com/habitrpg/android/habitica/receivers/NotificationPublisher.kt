package com.habitrpg.android.habitica.receivers

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.content.WakefulBroadcastReceiver

import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.TaskAlarmManager
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.MainActivity
import io.reactivex.functions.BiFunction

import javax.inject.Inject

import io.reactivex.functions.Consumer
import io.realm.RealmResults
import java.util.*


@Suppress("DEPRECATION")
//https://gist.github.com/BrandonSmith/6679223
class NotificationPublisher : WakefulBroadcastReceiver() {

    @Inject
    var taskRepository: TaskRepository? = null
    @Inject
    lateinit var userRepository: UserRepository
    private var context: Context? = null

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
        if (taskRepository == null) {
            HabiticaBaseApplication.component?.inject(this)
        }

        val checkDailies = intent.getBooleanExtra(CHECK_DAILIES, false)
        if (checkDailies) {
            //Maybe.zip(userRepository.getUser().firstElement(), taskRepository.getTasks(Task.TYPE_DAILY).firstElement())
            taskRepository?.getTasks(Task.TYPE_DAILY)?.firstElement()?.zipWith(userRepository.getUser().firstElement(), BiFunction<RealmResults<Task>, User, Pair<RealmResults<Task>, User>> { tasks, user ->
                return@BiFunction Pair(tasks, user)
            })?.subscribe(Consumer { pair ->
                var showNotifications = false
                for (task in pair.first) {
                    if (task?.checkIfDue() == true) {
                        showNotifications = true
                        break
                    }
                }
                TaskAlarmManager.scheduleDailyReminder(context)
                if (showNotifications) {
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

                    val id = intent.getIntExtra(NOTIFICATION_ID, 0)
                    notificationManager?.notify(id, buildNotification(pair.second.authentication?.timestamps?.createdAt))
                }
            }, RxErrorHandler.handleEmptyError())

        } else {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

            val id = intent.getIntExtra(NOTIFICATION_ID, 0)
            notificationManager?.notify(id, buildNotification())
        }
    }

    private fun buildNotification(registrationDate: Date? = null): Notification? {
        val thisContext = context ?: return null
        val notification: Notification
        val builder = Notification.Builder(thisContext)
        builder.setContentTitle(thisContext.getString(R.string.reminder_title))
        if (registrationDate != null) {
            val registrationCal = Calendar.getInstance()
            registrationCal.time = registrationDate
            val todayCal = Calendar.getInstance()
            val isSameDay = (registrationCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                    registrationCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)
                    )
            val isPreviousDay = (registrationCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                    registrationCal.get(Calendar.DAY_OF_YEAR) == (todayCal.get(Calendar.DAY_OF_YEAR) - 1)
                    )
            if (isSameDay) {
                builder.setContentTitle(thisContext.getString(R.string.same_day_reminder_title))
                builder.setContentText(thisContext.getString(R.string.same_day_reminder_text))
            } else if (isPreviousDay) {
                builder.setContentTitle(thisContext.getString(R.string.next_day_reminder_title))
                builder.setContentText(thisContext.getString(R.string.next_day_reminder_text))
            }
        }
        builder.setSmallIcon(R.drawable.ic_gryphon_white)
        val notificationIntent = Intent(thisContext, MainActivity::class.java)

        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val intent = PendingIntent.getActivity(thisContext, 0,
                notificationIntent, 0)
        builder.setContentIntent(intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(ContextCompat.getColor(thisContext, R.color.brand_300))
        }

        notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.build()
        } else {
            builder.notification
        }
        notification.defaults = notification.defaults or Notification.DEFAULT_LIGHTS

        notification.flags = notification.flags or (Notification.FLAG_AUTO_CANCEL or Notification.FLAG_SHOW_LIGHTS)
        return notification
    }

    companion object {

        var NOTIFICATION_ID = "notification-id"
        var CHECK_DAILIES = "check-dailies"
    }
}
