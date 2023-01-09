package com.habitrpg.android.habitica.helpers

import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.preference.PreferenceManager
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.extensions.withImmutableFlag
import com.habitrpg.android.habitica.models.tasks.RemindersItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.receivers.NotificationPublisher
import com.habitrpg.android.habitica.receivers.TaskReceiver
import com.habitrpg.shared.habitica.HLogger
import com.habitrpg.shared.habitica.LogLevel
import com.habitrpg.shared.habitica.models.tasks.TaskType
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

class TaskAlarmManager(
    private var context: Context,
    private var taskRepository: TaskRepository,
    private var userId: String
) {
    private val am: AlarmManager? = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    private fun setAlarmsForTask(task: Task) {
        task.reminders?.let {
            for (reminder in it) {
                var currentReminder = reminder
                if (task.type == TaskType.DAILY) {
                    // Ensure that we set to the next available time
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

    // This function is used from the TaskReceiver since we do not have access to the task
    // We currently only use this function to schedule the next reminder for dailies
    // We may be able to use repeating alarms instead of this in the future
    fun addAlarmForTaskId(taskId: String) {
        MainScope().launch(ExceptionHandler.coroutine()) {
            val task = taskRepository.getTaskCopy(taskId)
                .filter { task -> task.isValid && task.isManaged && TaskType.DAILY == task.type }
                .first()
            setAlarmsForTask(task)
        }
    }

    suspend fun scheduleAllSavedAlarms(preventDailyReminder: Boolean) {
        val tasks = taskRepository.getTaskCopies(userId).firstOrNull()
        tasks?.forEach { this.setAlarmsForTask(it) }

        if (!preventDailyReminder) {
            scheduleDailyReminder(context)
        }
    }

    fun scheduleAlarmsForTask(task: Task) {
        setAlarmsForTask(task)
    }

    private fun setTimeForDailyReminder(remindersItem: RemindersItem?, task: Task): RemindersItem? {
        val newTime = (remindersItem?.let { task.getNextReminderOccurrence(it) } ?: return null)

        remindersItem.time = newTime.withZoneSameLocal(ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        return remindersItem
    }

    /**
     * If reminderItem time is before now, a new reminder will not be created until the reminder passes.
     * The exception to this is if the task & reminder was newly created for the same time,
     * in which the alarm will be created -
     * which is indicated by first nextDue being null (As the alarm is created before the API returns nextDue times)
     */
    private fun setAlarmForRemindersItem(reminderItemTask: Task, remindersItem: RemindersItem?) {
        val now = ZonedDateTime.now().withZoneSameLocal(ZoneId.systemDefault())?.toInstant()
        val zonedTime = remindersItem?.getLocalZonedDateTimeInstant()
        if (remindersItem == null
            || (reminderItemTask.type == TaskType.DAILY && zonedTime?.isBefore(now) == true && reminderItemTask.nextDue?.firstOrNull() != null)
            || (reminderItemTask.type == TaskType.TODO && zonedTime?.isBefore(now) == true)
        ) {
            return
        }

        val time = Date.from(zonedTime)
        val cal = Calendar.getInstance()
        cal.time = time

        val intent = Intent(context, TaskReceiver::class.java)
        intent.action = remindersItem.id
        intent.putExtra(TASK_NAME_INTENT_KEY, reminderItemTask.text)
        intent.putExtra(TASK_ID_INTENT_KEY, reminderItemTask.id)

        val intentId = remindersItem.id?.hashCode() ?: (0 and 0xfffffff)
        // Cancel alarm if already exists
        val previousSender = PendingIntent.getBroadcast(
            context,
            intentId,
            intent,
            withImmutableFlag(PendingIntent.FLAG_NO_CREATE)
        )
        if (previousSender != null) {
            previousSender.cancel()
            am?.cancel(previousSender)
        }

        val sender = PendingIntent.getBroadcast(
            context,
            intentId,
            intent,
            withImmutableFlag(PendingIntent.FLAG_CANCEL_CURRENT)
        )

        setAlarm(context, cal.timeInMillis, sender)
    }

    private fun removeAlarmForRemindersItem(remindersItem: RemindersItem) {
        val intent = Intent(context, TaskReceiver::class.java)
        intent.action = remindersItem.id
        val intentId = remindersItem.id?.hashCode() ?: (0 and 0xfffffff)
        val sender = PendingIntent.getBroadcast(
            context,
            intentId,
            intent,
            withImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT)
        )
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        sender.cancel()
        am?.cancel(sender)
    }

    companion object {
        const val TASK_ID_INTENT_KEY = "TASK_ID"
        const val TASK_NAME_INTENT_KEY = "TASK_NAME"

        fun scheduleDailyReminder(context: Context?) {
            if (context == null) return
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            if (prefs.getBoolean("use_reminder", false)) {
                val timeval = prefs.getString("reminder_time", "19:00")

                val pieces =
                    timeval?.split(":".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
                        ?: return
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

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                val previousSender = PendingIntent.getBroadcast(
                    context,
                    0,
                    notificationIntent,
                    withImmutableFlag(PendingIntent.FLAG_NO_CREATE)
                )
                if (previousSender != null) {
                    previousSender.cancel()
                    alarmManager?.cancel(previousSender)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    notificationIntent,
                    withImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT)
                )

                setAlarm(context, triggerTime, pendingIntent)
            }
        }

        fun removeDailyReminder(context: Context?) {
            val notificationIntent = Intent(context, NotificationPublisher::class.java)
            val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val displayIntent =
                PendingIntent.getBroadcast(context, 0, notificationIntent, withImmutableFlag(0))
            alarmManager?.cancel(displayIntent)
        }

        private fun setAlarm(context: Context, time: Long, pendingIntent: PendingIntent?) {
            HLogger.log(LogLevel.INFO, "TaskAlarmManager", "Scheduling for $time")
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

            if (pendingIntent == null) {
                return
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                alarmManager?.setWindow(AlarmManager.RTC_WAKEUP, time, 60000, pendingIntent)
            } else {
                try {
                    alarmManager?.setAlarmClock(AlarmClockInfo(time, pendingIntent), pendingIntent)
                } catch (ex: Exception) {
                    when(ex) {
                        is IllegalStateException, is SecurityException -> {
                            alarmManager?.setWindow(AlarmManager.RTC_WAKEUP, time, 60000, pendingIntent)
                        }
                        else -> throw ex
                    }
                }
            }
        }
    }
}
