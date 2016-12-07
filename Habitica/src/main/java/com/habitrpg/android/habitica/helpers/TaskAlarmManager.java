package com.habitrpg.android.habitica.helpers;

import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.NotificationPublisher;
import com.habitrpg.android.habitica.events.ReminderDeleteEvent;
import com.habitrpg.android.habitica.events.TaskDeleteEvent;
import com.habitrpg.android.habitica.events.TaskSaveEvent;
import com.habitrpg.android.habitica.proxy.ifce.CrashlyticsProxy;
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
import android.os.Build;
import android.support.v7.preference.PreferenceManager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.schedulers.Schedulers;

import static android.os.Build.VERSION.SDK_INT;

/**
 * Created by keithholliday on 5/29/16.
 */
public class TaskAlarmManager {
    public static final String TASK_ID_INTENT_KEY = "TASK_ID";
    public static final String TASK_NAME_INTENT_KEY = "TASK_NAME";
    private static TaskAlarmManager instance = null;
    private Context context;
    private AlarmManager am;
    @Inject
    CrashlyticsProxy crashlyticsProxy;

    private TaskAlarmManager(Context context) {
        HabiticaBaseApplication.getComponent().inject(this);
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
        Observable.defer(() -> Observable.from(new Select()
                .from(Task.class)
                .queryList()))
                .doOnNext(this::setAlarmsForTask)
                .subscribeOn(Schedulers.io())
                .subscribe(task -> {},crashlyticsProxy::logException);

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

        setAlarm(context, cal.getTimeInMillis(), sender);

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
            cal.set(Calendar.SECOND, 0);
            if (cal.getTimeInMillis() < new Date().getTime()) {
                cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR)+1);
            }
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

            setAlarm(context, trigger_time, pendingIntent);
        }
    }

    public static void removeDailyReminder(Context context) {
        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent displayIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, 0);
        alarmManager.cancel(displayIntent);
    }

    private static void setAlarm(Context context, long time, PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (pendingIntent == null) {
            return;
        }

        if (SDK_INT < Build.VERSION_CODES.KITKAT)
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        else if (Build.VERSION_CODES.KITKAT <= SDK_INT && SDK_INT < Build.VERSION_CODES.M)
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, time, time+60000, pendingIntent);
        else if (SDK_INT >= Build.VERSION_CODES.M)
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }
}
