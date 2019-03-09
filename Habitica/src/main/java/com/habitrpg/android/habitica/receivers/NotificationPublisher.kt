package com.habitrpg.android.habitica.receivers

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.AmplitudeManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.TaskAlarmManager
import com.habitrpg.android.habitica.helpers.notifications.createOrUpdateHabiticaChannel
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.MainActivity
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.realm.RealmResults
import java.util.*
import javax.inject.Inject


@Suppress("DEPRECATION")
//https://gist.github.com/BrandonSmith/6679223
class NotificationPublisher : BroadcastReceiver() {

    @Inject
    lateinit var taskRepository: TaskRepository
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private var wasInjected = false
    private var context: Context? = null

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
        if (!wasInjected) {
            wasInjected = true
            HabiticaBaseApplication.component?.inject(this)
        }

        val additionalData = HashMap<String, Any>()
        additionalData["identifier"] = "daily_reminder"
        AmplitudeManager.sendEvent("receive notification", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData)

        var wasInactive = false
        //Show special notification if user hasn't logged in for a week
        if (sharedPreferences.getLong("lastAppLaunch", Date().time) < (Date().time - 604800000L)) {
            wasInactive = true
            sharedPreferences.edit { putBoolean("preventDailyReminder", true) }
        } else {
            TaskAlarmManager.scheduleDailyReminder(context)
        }
        val checkDailies = intent.getBooleanExtra(CHECK_DAILIES, false)
        if (checkDailies) {
            taskRepository.getTasks(Task.TYPE_DAILY).firstElement().zipWith(userRepository.getUser().firstElement(), BiFunction<RealmResults<Task>, User, Pair<RealmResults<Task>, User>> { tasks, user ->
                return@BiFunction Pair(tasks, user)
            }).subscribe(Consumer { pair ->
                var showNotifications = false
                for (task in pair.first) {
                    if (task?.checkIfDue() == true) {
                        showNotifications = true
                        break
                    }
                }
                if (showNotifications) {
                    notify(intent, buildNotification(wasInactive, pair.second.authentication?.timestamps?.createdAt))
                }
            }, RxErrorHandler.handleEmptyError())

        } else {
            notify(intent, buildNotification(wasInactive))
        }
    }

    private fun notify(intent: Intent, notification: Notification?) {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager?.createOrUpdateHabiticaChannel()
        }
        val id = intent.getIntExtra(NOTIFICATION_ID, 0)
        notificationManager?.notify(id, notification)
    }

    private fun buildNotification(wasInactive: Boolean, registrationDate: Date? = null): Notification? {
        val thisContext = context ?: return null
        val notification: Notification
        val builder = NotificationCompat.Builder(thisContext, "default")
        builder.setContentTitle(thisContext.getString(R.string.reminder_title))
        var notificationText = getRandomDailyTip()
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
                notificationText = thisContext.getString(R.string.same_day_reminder_text)
            } else if (isPreviousDay) {
                builder.setContentTitle(thisContext.getString(R.string.next_day_reminder_title))
                notificationText = thisContext.getString(R.string.next_day_reminder_text)
            }
        }
        if (wasInactive) {
            builder.setContentText(thisContext.getString(R.string.week_reminder_title))
            notificationText = thisContext.getString(R.string.week_reminder_text)
        }

        builder.setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))

        builder.setSmallIcon(R.drawable.ic_gryphon_white)
        val notificationIntent = Intent(thisContext, MainActivity::class.java)
        notificationIntent.putExtra("notificationIdentifier", "daily_reminder")

        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val intent = PendingIntent.getActivity(thisContext, 0,
                notificationIntent, 0)
        builder.setContentIntent(intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.color = ContextCompat.getColor(thisContext, R.color.brand_300)
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

    private fun getRandomDailyTip(): String {
        val thisContext = context ?: return ""
        val index = Random().nextInt(4)
        return when (index) {
            0 -> thisContext.getString(R.string.daily_tip_0)
            1 -> thisContext.getString(R.string.daily_tip_1)
            2 -> thisContext.getString(R.string.daily_tip_2)
            3 -> thisContext.getString(R.string.daily_tip_3)
            4 -> thisContext.getString(R.string.daily_tip_4)
            5 -> thisContext.getString(R.string.daily_tip_5)
            6 -> thisContext.getString(R.string.daily_tip_6)
            7 -> thisContext.getString(R.string.daily_tip_7)
            8 -> thisContext.getString(R.string.daily_tip_8)
            9 -> thisContext.getString(R.string.daily_tip_9)
            else -> ""
        }
    }

    companion object {

        var NOTIFICATION_ID = "notification-id"
        var CHECK_DAILIES = "check-dailies"
    }
}
