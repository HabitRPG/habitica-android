package com.habitrpg.android.habitica.receivers

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.extensions.withImmutableFlag
import com.habitrpg.android.habitica.helpers.TaskAlarmManager
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.shared.habitica.HLogger
import com.habitrpg.shared.habitica.LogLevel
import com.habitrpg.shared.habitica.models.tasks.TaskType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TaskReceiver : BroadcastReceiver() {
    @Inject
    lateinit var taskAlarmManager: TaskAlarmManager

    @Inject
    lateinit var taskRepository: TaskRepository

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        HLogger.log(LogLevel.INFO, this::javaClass.name, "onReceive")
        val extras = intent.extras
        if (extras != null) {
            val taskId = extras.getString(TaskAlarmManager.TASK_ID_INTENT_KEY)
            // This will set up the next reminders for dailies
            if (taskId != null) {
                taskAlarmManager.addAlarmForTaskId(taskId)
            }

            MainScope().launch(ExceptionHandler.coroutine()) {
                val task = taskRepository.getTask(taskId ?: "").firstOrNull() ?: return@launch
                if (task.isUpdatedToday && task.completed) {
                    return@launch
                }
                createNotification(context, task)
            }
        }
    }

    private fun createNotification(
        context: Context,
        task: Task
    ) {
        val intent = Intent(context, MainActivity::class.java)
        HLogger.log(LogLevel.INFO, this::javaClass.name, "Create Notification")

        intent.putExtra("notificationIdentifier", "task_reminder")
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),
                intent,
                withImmutableFlag(0)
            )
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        var notificationBuilder =
            NotificationCompat.Builder(context, "default")
                .setSmallIcon(R.drawable.ic_gryphon_white)
                .setColor(ContextCompat.getColor(context, R.color.brand_300))
                .setContentTitle(task.text)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(task.notes)
                )
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(soundUri)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationBuilder = notificationBuilder.setCategory(Notification.CATEGORY_REMINDER)
        }

        if (task.type == TaskType.DAILY || task.type == TaskType.TODO) {
            val completeIntent =
                Intent(context, LocalNotificationActionReceiver::class.java).apply {
                    action = context.getString(R.string.complete_task_action)
                    putExtra("taskID", task.id)
                    putExtra("NOTIFICATION_ID", task.id.hashCode())
                }
            val pendingIntentComplete =
                PendingIntent.getBroadcast(
                    context,
                    task.id.hashCode(),
                    completeIntent,
                    withImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT)
                )
            notificationBuilder.addAction(
                0,
                context.getString(R.string.complete),
                pendingIntentComplete
            )
        }
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(task.id.hashCode(), notificationBuilder.build())
    }
}
