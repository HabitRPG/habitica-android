package com.habitrpg.android.habitica.helpers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.models.tasks.RemindersItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.receivers.NotificationPublisher
import com.habitrpg.android.habitica.receivers.TaskReceiver
import io.reactivex.Flowable
import io.reactivex.functions.Consumer
import java.util.*

class TaskAlarmManager(private var context: Context, private var taskRepository: TaskRepository, private var userId: String) {
    private val am: AlarmManager? = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    private fun setAlarmsForTask(task: Task) {
        task.reminders?.let {
            for (reminder in it) {
                var currentReminder = reminder
                if (task.type == Task.TYPE_DAILY) {
                    //Ensure that we set to the next available time
                    currentReminder = this.setTimeForDailyReminder(currentReminder, task)
                }
                this.setAlarmForRemindersItem(task, currentReminder)
            }
        }
    }

    private fun removeAlarmsForTask(task: Task) {
        task.reminders?.let {
            for (reminder in it) {
                this.removeAlarmForRemindersItem(reminder)
            }
        }
    }

    //This function is used from the TaskReceiver since we do not have access to the task
    //We currently only use this function to schedule the next reminder for dailies
    //We may be able to use repeating alarms instead of this in the future
    fun addAlarmForTaskId(taskId: String) {
        taskRepository.getTaskCopy(taskId)
                .filter { task -> task.isValid && task.isManaged && Task.TYPE_DAILY == task.type }
                .firstElement()
                .subscribe(Consumer { this.setAlarmsForTask(it) }, RxErrorHandler.handleEmptyError())
    }

    fun scheduleAllSavedAlarms(preventDailyReminder: Boolean) {
        taskRepository.getTaskCopies(userId)
                .firstElement()
                .toFlowable()
                .flatMap<Task> { Flowable.fromIterable(it) }
                .subscribe(Consumer { this.setAlarmsForTask(it) }, RxErrorHandler.handleEmptyError())

        if (!preventDailyReminder) {
            scheduleDailyReminder(context)
        }
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putLong("lastReminderSchedule", Date().time)
        }
    }

    fun scheduleAlarmsForTask(task: Task) {
        setAlarmsForTask(task)
    }

    private fun setTimeForDailyReminder(remindersItem: RemindersItem?, task: Task): RemindersItem? {
        val oldTime = remindersItem?.time
        val newTime = task.getNextReminderOccurence(oldTime) ?: return null
        val calendar = Calendar.getInstance()
        calendar.time = newTime
        @Suppress("DEPRECATION")
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), oldTime?.hours ?: 0, oldTime?.minutes ?: 0, 0)
        remindersItem?.time = calendar.time
        return remindersItem
    }

    private fun setAlarmForRemindersItem(reminderItemTask: Task, remindersItem: RemindersItem?) {
        val now = Date()
        if (remindersItem == null || remindersItem.time?.before(now) == true) {
            return
        }

        val cal = Calendar.getInstance()
        cal.time = remindersItem.time

        val intent = Intent(context, TaskReceiver::class.java)
        intent.action = remindersItem.id
        intent.putExtra(TASK_NAME_INTENT_KEY, reminderItemTask.text)
        intent.putExtra(TASK_ID_INTENT_KEY, reminderItemTask.id)

        val intentId = remindersItem.id?.hashCode() ?: 0 and 0xfffffff
        //Cancel alarm if already exists
        val previousSender = PendingIntent.getBroadcast(context, intentId, intent, PendingIntent.FLAG_NO_CREATE)
        if (previousSender != null) {
            previousSender.cancel()
            am?.cancel(previousSender)
        }

        val sender = PendingIntent.getBroadcast(context, intentId, intent, PendingIntent.FLAG_CANCEL_CURRENT)

        setAlarm(context, cal.timeInMillis, sender)

        taskRepository.saveReminder(remindersItem)
    }

    private fun removeAlarmForRemindersItem(remindersItem: RemindersItem) {
        val intent = Intent(context, TaskReceiver::class.java)
        intent.action = remindersItem.id
        val intentId = remindersItem.id?.hashCode() ?: 0 and 0xfffffff
        val sender = PendingIntent.getBroadcast(context, intentId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        sender.cancel()
        am?.cancel(sender)
    }

    companion object {
        const val TASK_ID_INTENT_KEY = "TASK_ID"
        const val TASK_NAME_INTENT_KEY = "TASK_NAME"

        fun scheduleDailyReminder(context: Context?) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            if (prefs.getBoolean("use_reminder", false)) {
                val timeval = prefs.getString("reminder_time", "19:00")

                val pieces = timeval?.split(":".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray() ?: return
                val hour = Integer.parseInt(pieces[0])
                val minute = Integer.parseInt(pieces[1])
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                cal.set(Calendar.SECOND, 0)
                if (cal.timeInMillis < Date().time) {
                    cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1)
                }
                val triggerTime = cal.timeInMillis

                val notificationIntent = Intent(context, NotificationPublisher::class.java)
                notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1)
                notificationIntent.putExtra(NotificationPublisher.CHECK_DAILIES, false)

                val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                val previousSender = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_NO_CREATE)
                if (previousSender != null) {
                    previousSender.cancel()
                    alarmManager?.cancel(previousSender)
                }

                val pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                if (context != null) {
                    setAlarm(context, triggerTime, pendingIntent)
                }
            }
        }

        fun removeDailyReminder(context: Context?) {
            val notificationIntent = Intent(context, NotificationPublisher::class.java)
            val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val displayIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, 0)
            alarmManager?.cancel(displayIntent)
        }

        private fun setAlarm(context: Context, time: Long, pendingIntent: PendingIntent?) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

            if (pendingIntent == null) {
                return
            }

            if (SDK_INT < Build.VERSION_CODES.KITKAT) {
                alarmManager?.set(AlarmManager.RTC_WAKEUP, time, pendingIntent)
            } else if (Build.VERSION_CODES.KITKAT <= SDK_INT && SDK_INT < Build.VERSION_CODES.M) {
                alarmManager?.setWindow(AlarmManager.RTC_WAKEUP, time, time + 60000, pendingIntent)
            } else if (SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager?.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
            }
        }
    }
}
