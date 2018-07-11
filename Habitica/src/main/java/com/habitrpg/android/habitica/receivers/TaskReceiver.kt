package com.habitrpg.android.habitica.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.habitrpg.android.habitica.HabiticaApplication
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.TaskAlarmManager
import com.habitrpg.android.habitica.ui.activities.MainActivity
import javax.inject.Inject


class TaskReceiver : BroadcastReceiver() {

    @Inject
    lateinit var taskAlarmManager: TaskAlarmManager

    override fun onReceive(context: Context, intent: Intent) {
        HabiticaBaseApplication.component?.inject(this)
        val extras = intent.extras
        if (extras != null) {
            val taskTitle = extras.getString(TaskAlarmManager.TASK_NAME_INTENT_KEY)
            val taskId = extras.getString(TaskAlarmManager.TASK_ID_INTENT_KEY)
            //This will set up the next reminders for dailies
            if (taskId != null) {
                taskAlarmManager.addAlarmForTaskId(taskId)
            }
            createNotification(context, taskTitle)
        }
    }

    private fun createNotification(context: Context, taskTitle: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent, 0)
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(context, "default")
                .setSmallIcon(R.drawable.ic_gryphon_white)
                .setContentTitle(taskTitle)
                .setContentText(taskTitle)
                .setSound(soundUri)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /* Create or update. */
            val channel = NotificationChannel("default", "Habitica Notifications", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager?.createNotificationChannel(channel)
        }
        notificationManager?.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}