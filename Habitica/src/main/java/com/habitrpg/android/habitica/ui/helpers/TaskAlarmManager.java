package com.habitrpg.android.habitica.ui.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.habitrpg.android.habitica.events.TaskCreatedEvent;
import com.habitrpg.android.habitica.events.TaskSaveEvent;
import com.habitrpg.android.habitica.events.TaskUpdatedEvent;
import com.habitrpg.android.habitica.receivers.TodoReceiver;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.RemindersItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by keithholliday on 5/29/16.
 */
public class TaskAlarmManager {
    private static TaskAlarmManager instance = null;

    private Context context;

    public static final String TASK_ID_INTENT_KEY = "TASK_ID";
    public static final String TASK_NAME_INTENT_KEY = "TASK_NAME";

    private TaskAlarmManager(Context context) {
        this.context = context;
        EventBus.getDefault().register(this);
    }

    public static TaskAlarmManager getInstance(Context context){
        if(instance == null)
        {
            instance = new TaskAlarmManager(context);
        }
        return instance;
    }

    @Subscribe
    public void onEvent(TaskSaveEvent evnt) {
        Task task = (Task) evnt.task;
        List<RemindersItem> reminders = task.getReminders();
        for (RemindersItem reminder : reminders) {
            this.addAlarmForTaskReminder(task, reminder);
        }
    }

    public void addAlarmForTaskReminder(Task task, RemindersItem reminder) {
        if (task.getType().equals("todo")) {
            this.addAlarmForTodo(task, reminder);
        }
    }

    private void addAlarmForTodo(Task task, RemindersItem reminder) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(reminder.getStartDate());

        Intent intent = new Intent(context, TodoReceiver.class);
        intent.putExtra(TASK_NAME_INTENT_KEY, task.getText());
        intent.putExtra(TASK_ID_INTENT_KEY, task.getId());

        Integer alarmId = reminder.getAlarmId();
        if (alarmId == null) {
            alarmId = (int) System.currentTimeMillis();
            reminder.setAlarmId(alarmId);
        }

        PendingIntent sender = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);

        reminder.async().save();
    }

    public void scheduleAllSavedAlarms() {
        List<RemindersItem> reminders = new Select()
                .from(RemindersItem.class)
                .where(Condition.column("time").greaterThan(new Date()))
                .orderBy(true, "time")
                .queryList();

        for (RemindersItem remindersItem : reminders) {
            Task reminderItemTask = remindersItem.getTask();
            Integer alarmId = remindersItem.getAlarmId();

            Calendar cal = Calendar.getInstance();
            cal.setTime(remindersItem.getStartDate());

            Intent intent = new Intent(context, TodoReceiver.class);
            intent.putExtra(TASK_NAME_INTENT_KEY, reminderItemTask.getText());
            intent.putExtra(TASK_ID_INTENT_KEY, reminderItemTask.getId());

            PendingIntent sender = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
        }
    }

}
