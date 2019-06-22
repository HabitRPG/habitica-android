package com.habitrpg.android.habitica.receivers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.helpers.AmplitudeManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.TaskAlarmManager
import com.habitrpg.android.habitica.helpers.notifications.createOrUpdateHabiticaChannel
import com.habitrpg.android.habitica.ui.activities.MainActivity
import io.reactivex.functions.Consumer
import java.util.*
import javax.inject.Inject


class TaskReceiver : BroadcastReceiver() {

    @Inject
    lateinit var taskAlarmManager: TaskAlarmManager

    @Inject
    lateinit var taskRepository: TaskRepository

    override fun onReceive(context: Context, intent: Intent) {
        HabiticaBaseApplication.userComponent?.inject(this)
        val extras = intent.extras
        if (extras != null) {
            val taskTitle = extras.getString(TaskAlarmManager.TASK_NAME_INTENT_KEY)
            val taskId = extras.getString(TaskAlarmManager.TASK_ID_INTENT_KEY)
            //This will set up the next reminders for dailies
            if (taskId != null) {
                taskAlarmManager.addAlarmForTaskId(taskId)
            }

            taskRepository.getTask(taskId ?: "")
                    .firstElement()
                    .subscribe(Consumer {
                        if (it.isValid && it.completed) {
                            return@Consumer
                        }

                        val additionalData = HashMap<String, Any>()
                        additionalData["identifier"] = "task_reminder"
                        AmplitudeManager.sendEvent("receive notification", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData)

                        createNotification(context, taskTitle)
                    }, RxErrorHandler.handleEmptyError())
        }
    }

    private fun createNotification(context: Context, taskTitle: String?) {
        val intent = Intent(context, MainActivity::class.java)

        intent.putExtra("notificationIdentifier", "task_reminder")
        val pendingIntent = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent, 0)
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(context, "default")
                .setSmallIcon(R.drawable.ic_gryphon_white)
                .setContentTitle(taskTitle)
                .setSound(soundUri)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /* Create or update. */
            notificationManager?.createOrUpdateHabiticaChannel()
        }
        notificationManager?.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}