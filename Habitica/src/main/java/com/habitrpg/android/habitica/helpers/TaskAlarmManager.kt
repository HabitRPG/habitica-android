package com.habitrpg.android.habitica.helpers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.preference.PreferenceManager
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.extensions.withImmutableFlag
import com.habitrpg.android.habitica.models.tasks.RemindersItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import com.habitrpg.android.habitica.receivers.NotificationPublisher
import com.habitrpg.android.habitica.receivers.TaskReceiver
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.shared.habitica.HLogger
import com.habitrpg.shared.habitica.LogLevel
import com.habitrpg.shared.habitica.models.tasks.TaskType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.DateTimeException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

class TaskAlarmManager(
    private var context: Context,
    private var taskRepository: TaskRepository,
    private var authenticationHandler: AuthenticationHandler,
) {
    private val am: AlarmManager? = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    private val upcomingReminderOccurrencesToSchedule = 3

    /**
     * Schedules multiple alarms for each reminder associated with a given task.
     *
     * This method iterates through all reminders of a task and schedules multiple upcoming alarms for each.
     * It determines the upcoming reminder times based on the reminder's configuration (like frequency, repeat days, etc.)
     * and schedules an alarm for each of these times.
     *
     * For each reminder, it updates the reminder's time to each upcoming occurrence and then calls
     * `setAlarmForRemindersItem` to handle the actual alarm scheduling. This ensures that each reminder
     * is scheduled accurately according to its specified rules and times.
     *
     * This method is particularly useful for reminders that need to be repeated at regular intervals
     * (e.g., daily, weekly) or on specific days, as it schedules multiple occurrences in advance.
     *
     * @param task The task for which the alarms are being set. Each reminder in the task's reminder list
     *             is processed to schedule the upcoming alarms.
     */
    private fun setAlarmsForTask(task: Task) {
        CoroutineScope(Dispatchers.IO).launch {
            val reminderOccurencesToSchedule =
                if (task.type == TaskType.TODO) {
                    1
                } else {
                    // For dailies, we schedule multiple reminders in advance
                    upcomingReminderOccurrencesToSchedule
                }
            task.reminders?.let { reminders ->
                for (reminder in reminders) {
                    try {
                        val upcomingReminders =
                            task.getNextReminderOccurrences(reminder, reminderOccurencesToSchedule)
                        upcomingReminders?.forEachIndexed { index, reminderNextOccurrenceTime ->
                            reminder?.time =
                                reminderNextOccurrenceTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            setAlarmForRemindersItem(task, reminder, index)
                        }
                    } catch (_: DateTimeException) {
                        // code accidentally generated an invalid date
                    }
                }
            }
        }
    }

    fun removeAlarmsForTask(task: Task) {
        CoroutineScope(Dispatchers.IO).launch {
            task.reminders?.let { reminders ->
                // Remove not only the immediate reminder, but also the next however many (upcomingReminderOccurrencesToSchedule) reminders
                reminders.forEachIndexed { index, reminder ->
                    removeAlarmForRemindersItem(reminder, index)
                }
            }
        }
    }

    // This function is used from the TaskReceiver since we do not have access to the task
    // We currently only use this function to schedule the next reminder for dailies
    // We may be able to use repeating alarms instead of this in the future
    fun addAlarmForTaskId(taskId: String) {
        MainScope().launch(ExceptionHandler.coroutine()) {
            val task =
                taskRepository.getTaskCopy(taskId)
                    .filter { task -> task.isValid && task.isManaged && TaskType.DAILY == task.type }
                    .first()
            setAlarmsForTask(task)
        }
    }

    suspend fun scheduleAllSavedAlarms(preventDailyReminder: Boolean) {
        val tasks = taskRepository.getTaskCopies().firstOrNull()
        tasks?.forEach { this.setAlarmsForTask(it) }

        if (!preventDailyReminder) {
            scheduleDailyReminder(context)
        }
    }

    fun scheduleAlarmsForTask(task: Task) {
        setAlarmsForTask(task)
    }

    /**
     * Schedules an alarm for a given reminder associated with a task.
     *
     * This method takes a task and its associated reminder item to schedule an alarm.
     * It first checks if the reminder time is valid and not in the past. If the reminder time
     * is valid, it prepares an intent for the alarm, uniquely identified by combining the reminder's ID
     * and its scheduled time. This unique identifier ensures that each reminder occurrence is distinctly handled.
     *
     * If an alarm with the same identifier already exists, it is cancelled and replaced with the new one.
     * This ensures that reminders are always up to date with their latest scheduled times.
     *
     * The alarm is scheduled to trigger at the exact time specified in the reminder. Upon triggering,
     * it will send a broadcast to `TaskReceiver`, which should handle the reminder notification.
     *
     * @param reminderItemTask The task associated with the reminder.
     * @param remindersItem The reminder item containing details like ID and the time for the reminder.
     *                      If this is null, the method returns immediately without scheduling an alarm.
     */
    private fun setAlarmForRemindersItem(
        reminderItemTask: Task,
        remindersItem: RemindersItem?,
        occurrenceIndex: Int,
    ) {
        if (remindersItem == null) return

        val now = ZonedDateTime.now().withZoneSameLocal(ZoneId.systemDefault())?.toInstant()
        val reminderZonedTime = remindersItem.getLocalZonedDateTimeInstant()

        if (reminderZonedTime == null || reminderZonedTime.isBefore(now)) {
            return
        }

        val intent = Intent(context, TaskReceiver::class.java)
        intent.action = remindersItem.id
        intent.putExtra(TASK_NAME_INTENT_KEY, reminderItemTask.text)
        intent.putExtra(TASK_ID_INTENT_KEY, reminderItemTask.id)

        // Create a unique identifier based on remindersItem.id and the occurrence index
        val intentId = (remindersItem.id?.hashCode() ?: 0) + occurrenceIndex

        // Cancel alarm if already exists
        val previousSender =
            PendingIntent.getBroadcast(
                context,
                intentId,
                intent,
                withImmutableFlag(PendingIntent.FLAG_NO_CREATE),
            )
        if (previousSender != null) {
            previousSender.cancel()
            am?.cancel(previousSender)
        }

        val sender =
            PendingIntent.getBroadcast(
                context,
                intentId,
                intent,
                withImmutableFlag(PendingIntent.FLAG_CANCEL_CURRENT),
            )

        CoroutineScope(Dispatchers.IO).launch {
            setAlarm(context, reminderZonedTime.toEpochMilli(), sender)
        }
    }

    private fun removeAlarmForRemindersItem(
        remindersItem: RemindersItem,
        occurrenceIndex: Int? = null,
    ) {
        val intent = Intent(context, TaskReceiver::class.java)
        intent.action = remindersItem.id
        val intentId =
            if (occurrenceIndex != null) {
                (
                    remindersItem.id?.hashCode()
                        ?: (0 and 0xfffffff)
                ) + occurrenceIndex
            } else {
                (
                    remindersItem.id?.hashCode()
                        ?: (0 and 0xfffffff)
                )
            }
        val sender =
            PendingIntent.getBroadcast(
                context,
                intentId,
                intent,
                withImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT),
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
                val previousSender =
                    PendingIntent.getBroadcast(
                        context,
                        0,
                        notificationIntent,
                        withImmutableFlag(PendingIntent.FLAG_NO_CREATE),
                    )
                if (previousSender != null) {
                    previousSender.cancel()
                    alarmManager?.cancel(previousSender)
                }

                val pendingIntent =
                    PendingIntent.getBroadcast(
                        context,
                        0,
                        notificationIntent,
                        withImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT),
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

        private fun setAlarm(
            context: Context,
            time: Long,
            pendingIntent: PendingIntent?,
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

            if (pendingIntent == null || alarmManager == null) {
                return
            }

            val notificationType = AlarmManager.RTC_WAKEUP
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    var canScheduleExact = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        canScheduleExact = alarmManager.canScheduleExactAlarms()
                    }
                    if (canScheduleExact) {
                        alarmManager.setExactAndAllowWhileIdle(notificationType, time, pendingIntent)
                        HLogger.log(LogLevel.DEBUG,
                            "TaskAlarmManager",
                            "setAlarm: Scheduling for $time using setExact ${Date().time}",
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(notificationType, time, pendingIntent)
                        HLogger.log(LogLevel.DEBUG,
                            "TaskAlarmManager",
                            "setAlarm: Scheduling for $time using setAndAllowWhileIdle",
                        )
                    }
                } catch (ex: Exception) {
                    when (ex) {
                        is IllegalStateException, is SecurityException -> {
                            alarmManager.setWindow(
                                notificationType,
                                time,
                                600000,
                                pendingIntent,
                            )
                        }

                        else -> {
                            throw ex
                        }
                    }
                }
            } else {
                alarmManager.set(notificationType, time, pendingIntent)
            }
        }
    }
}
