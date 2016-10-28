package com.habitrpg.android.habitica.helpers;

import com.habitrpg.android.habitica.NotificationPublisher;
import com.habitrpg.android.habitica.events.ReminderDeleteEvent;
import com.habitrpg.android.habitica.events.TaskDeleteEvent;
import com.habitrpg.android.habitica.events.TaskSaveEvent;
import com.habitrpg.android.habitica.receivers.TaskReceiver;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.RemindersItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by keithholliday on 5/29/16.
 */
public class TaskAlarmManager {
    public static final String TASK_ID_INTENT_KEY = "TASK_ID";
    public static final String TASK_NAME_INTENT_KEY = "TASK_NAME";
    private static TaskAlarmManager instance = null;
    private Context context;
    private AlarmManager am;

    private TaskAlarmManager(Context context) {
        this.context = context;
        EventBus.getDefault().register(this);
        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public static TaskAlarmManager getInstance(Context context) {
        if (instance == null) {
            instance = new TaskAlarmManager(context);
        }
        return instance;
    }

    @Subscribe
    public void onEvent(TaskSaveEvent event) {
        Task task = event.task;
        this.setAlarmsForTask(task);
    }

    @Subscribe
    public void onEvent(TaskDeleteEvent event) {
        Task task = event.task;
        this.removeAlarmsForTask(task);
    }


    @Subscribe
    public void onEvent(ReminderDeleteEvent event) {
        RemindersItem remindersItem = event.reminder;
        this.removeAlarmForRemindersItem(remindersItem);
    }

    public void setAlarmsForTask(Task task) {
        List<RemindersItem> reminders = task.getReminders();
        for (RemindersItem reminder : reminders) {
            if (task.getType().equals(Task.TYPE_DAILY)) {
                //Ensure that we set to the next available time
                reminder = this.setTimeForDailyReminder(reminder, task);
            }
            this.setAlarmForRemindersItem(reminder);
        }
    }

    public void removeAlarmsForTask(Task task) {
        List<RemindersItem> reminders = task.getReminders();
        for (RemindersItem reminder : reminders) {
            this.removeAlarmForRemindersItem(reminder);
        }
    }

    //This function is used from the TaskReceiver since we do not have access to the task
    //We currently only use this function to schedule the next reminder for dailies
    //We may be able to use repeating alarms instead of this in the future
    public void addAlarmForTaskId(String taskId) {
        List<Task> tasks = new Select()
                .from(Task.class)
                .where(Condition.column("id").eq(taskId))
                .queryList();

        if (tasks.size() == 0) return;
        Task task = tasks.get(0);

        if (!task.getType().equals(Task.TYPE_DAILY)) {
            return;
        }

        this.setAlarmsForTask(task);
    }

    public void scheduleAllSavedAlarms() {
        List<Task> tasks = new Select()
                .from(Task.class)
                .queryList();

        for (Task task : tasks) {
            this.setAlarmsForTask(task);
        }

        scheduleDailyReminder(context);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putLong("lastReminderSchedule", new Date().getTime());
        editor.apply();
    }

    private RemindersItem setTimeForDailyReminder(RemindersItem remindersItem, Task task) {
        Calendar calendar = Calendar.getInstance();
        Date oldTime = remindersItem.getTime();
        Date newTime = task.getNextActiveDateAfter(oldTime);
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), oldTime.getHours(), oldTime.getMinutes(), 0);
        remindersItem.setTime(newTime);
        return remindersItem;
    }

    private void setAlarmForRemindersItem(RemindersItem remindersItem) {
        Task reminderItemTask = remindersItem.getTask();

        Date now = new Date();
        if (remindersItem.getTime().before(now)) {
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(remindersItem.getTime());

        Intent intent = new Intent(context, TaskReceiver.class);
        intent.setAction(remindersItem.getId());
        intent.putExtra(TASK_NAME_INTENT_KEY, reminderItemTask.getText());
        intent.putExtra(TASK_ID_INTENT_KEY, reminderItemTask.getId());

        int intentId = remindersItem.getId().hashCode() & 0xfffffff;
        //Cancel alarm if already exists
        PendingIntent previousSender = PendingIntent.getBroadcast(context, intentId, intent, PendingIntent.FLAG_NO_CREATE);
        if (previousSender != null) {
            previousSender.cancel();
            am.cancel(previousSender);
        }

        PendingIntent sender = PendingIntent.getBroadcast(context, intentId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);

        remindersItem.save();
    }

    private void removeAlarmForRemindersItem(RemindersItem remindersItem) {
        Intent intent = new Intent(context, TaskReceiver.class);
        intent.setAction(remindersItem.getId());
        int intentId = remindersItem.getId().hashCode() & 0xfffffff;
        PendingIntent sender = PendingIntent.getBroadcast(context, intentId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        sender.cancel();
        am.cancel(sender);

    }

    public static void scheduleDailyReminder(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean("use_reminder", false)) {

            String timeval = prefs.getString("reminder_time", "19:00");

            String[] pieces = timeval.split(":");
            int hour = Integer.parseInt(pieces[0]);
            int minute = Integer.parseInt(pieces[1]);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            long trigger_time = cal.getTimeInMillis();

            Intent notificationIntent = new Intent(context, NotificationPublisher.class);
            notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
            notificationIntent.putExtra(NotificationPublisher.CHECK_DAILIES, false);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent previousSender = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_NO_CREATE);
            if (previousSender != null) {
                previousSender.cancel();
                alarmManager.cancel(previousSender);
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            alarmManager.set(AlarmManager.RTC_WAKEUP, trigger_time, pendingIntent);
        }
    }

    public static void removeDailyReminder(Context context) {
        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent displayIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, 0);
        alarmManager.cancel(displayIntent);
    }
}
