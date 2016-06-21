package com.habitrpg.android.habitica.ui.helpers;

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

    private TaskAlarmManager(Context context) {
        this.context = context;
        EventBus.getDefault().register(this);
    }

    public static TaskAlarmManager getInstance(Context context) {
        if (instance == null) {
            instance = new TaskAlarmManager(context);
        }
        return instance;
    }

    @Subscribe
    public void onEvent(TaskSaveEvent evnt) {
        Task task = (Task) evnt.task;
        List<RemindersItem> reminders = task.getReminders();
        for (RemindersItem reminder : reminders) {
            if (task.getType().equals(Task.TYPE_DAILY)) {
                //Ensure that we set to the next available time
                reminder = this.setTimeForDailyReminder(reminder, task);
            }
            this.setAlarmForRemindersItem(reminder);
            reminder.async().save();
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

        Task task = tasks.get(0);
        if (task == null) return;

        if (!task.getType().equals(Task.TYPE_DAILY)) {
            return;
        }

        List<RemindersItem> reminders = task.getReminders();
        for (RemindersItem remindersItem : reminders) {
            //Ensure that we set to the next available time
            remindersItem = this.setTimeForDailyReminder(remindersItem, task);
            this.setAlarmForRemindersItem(remindersItem);
            remindersItem.async().save();
        }
    }

    public void scheduleAllSavedAlarms() {
        List<RemindersItem> reminders = new Select()
                .from(RemindersItem.class)
                .where(Condition.column("time").greaterThan(new Date()))
                .orderBy(true, "time")
                .queryList();

        for (RemindersItem remindersItem : reminders) {
            this.setAlarmForRemindersItem(remindersItem);
        }
    }

    private RemindersItem setTimeForDailyReminder(RemindersItem remindersItem, Task task) {
        Date oldTime = remindersItem.getTime();
        Date newTime = task.getNextActiveDateAfter(oldTime);
        newTime.setHours(oldTime.getHours());
        newTime.setMinutes(oldTime.getMinutes());
        newTime.setSeconds(0);
        remindersItem.setTime(newTime);
        return remindersItem;
    }

    private void setAlarmForRemindersItem(RemindersItem remindersItem) {
        Task reminderItemTask = remindersItem.getTask();
        Integer alarmId = remindersItem.getAlarmId();

        if (alarmId == null) {
            alarmId = (int) System.currentTimeMillis();
            remindersItem.setAlarmId(alarmId);
        }

        Date now = new Date();

        if (remindersItem.getTime().before(now)) {
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(remindersItem.getTime());

        Intent intent = new Intent(context, TaskReceiver.class);
        intent.putExtra(TASK_NAME_INTENT_KEY, reminderItemTask.getText());
        intent.putExtra(TASK_ID_INTENT_KEY, reminderItemTask.getId());

        PendingIntent sender = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
    }
}
