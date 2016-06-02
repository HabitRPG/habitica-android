package com.habitrpg.android.habitica.ui.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.habitrpg.android.habitica.events.TaskCreatedEvent;
import com.habitrpg.android.habitica.events.TaskUpdatedEvent;
import com.habitrpg.android.habitica.receivers.TodoReceiver;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.RemindersItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;


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
    private Map<String, String> savedAlarms;

    public static final String TASK_ALARMS_PREFERENCE_KEY = "TaskAlarms";
    public static final String TASK_ALARMS_KEY = "task_alarms";
    public static final String TASK_ID_INTENT_KEY = "TASK_ID";
    public static final String TASK_NAME_INTENT_KEY = "TASK_NAME";

    private class TaskAlarm {
        private String taskId;
        private int alarmId;
        private String taskText;
        private Date taskDueDate;

        public TaskAlarm(String taskId, int alarmId) {
            this.taskId = taskId;
            this.alarmId = alarmId;
        }

        public void setTaskId (String taskId) {
            this.taskId = taskId;
        }

        public String getTaskId () {
            return this.taskId;
        }

        public void setTaskText (String taskText) {
            this.taskText = taskText;
        }

        public String getTaskText () {
            return this.taskText;
        }

        public void setTaskDueDate (Date taskDueDate) {
            this.taskDueDate = taskDueDate;
        }

        public Date getTaskDueDate () {
            return this.taskDueDate;
        }

        public void setAlarmId (int alarmId) {
            this.alarmId = alarmId;
        }

        public int getAlarmId () {
            return this.alarmId;
        }

        public String toStirng() {
            Gson gson = new Gson();
            String json = gson.toJson(this);
            return json;
        }
    }

    private TaskAlarmManager(Context context) {
        this.context = context;
        this.savedAlarms = this.loadAlarmsFromPreferences();
        this.savedAlarms = new HashMap<String, String>();
        this.saveAlarmsToPreferences(this.savedAlarms);
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
    public void onEvent(TaskCreatedEvent evnt) {
        Task task = (Task) evnt.task;
        List<RemindersItem> reminders = task.getReminders();

        for (RemindersItem reminder : reminders) {
            this.addAlarmForTaskReminder(task, reminder);
        }
    }

    @Subscribe
    public void onEvent(TaskUpdatedEvent evnt) {
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

    public void removeAlarmForTask(String taskId) {
        String taskAlarmJson = this.savedAlarms.get(taskId);
        if (taskAlarmJson != null) {
            this.savedAlarms.remove(taskId);
            this.saveAlarmsToPreferences(this.savedAlarms);
        }
    }

    private void addAlarmForTodo(Task task, RemindersItem reminder) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(reminder.getStartDate());
        //@TODO: Set Time
        cal.add(Calendar.SECOND, 60);

        Intent intent = new Intent(context, TodoReceiver.class);
        intent.putExtra(TASK_NAME_INTENT_KEY, task.getText());
        intent.putExtra(TASK_ID_INTENT_KEY, task.getId());

        String taskAlarmJson = this.savedAlarms.get(task.getId());

        int _id;
        if (taskAlarmJson == null) {
            _id = (int) System.currentTimeMillis();
            TaskAlarm taskAlarm = new TaskAlarm(task.getId(), _id);
            taskAlarm.setTaskText((task.getText()));
            taskAlarm.setTaskDueDate(task.getDueDate());
            this.savedAlarms.put(task.getId(), taskAlarm.toStirng());
        } else {
            Gson gson = new Gson();
            TaskAlarm taskAlarm = gson.fromJson(taskAlarmJson, TaskAlarm.class);
            taskAlarm.setTaskDueDate(task.getDueDate());
            _id = taskAlarm.getAlarmId();
        }

        PendingIntent sender = PendingIntent.getBroadcast(context, _id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);

        this.saveAlarmsToPreferences(this.savedAlarms);
    }

    public void scheduleAllSavedAlarms() {
        Iterator it = this.savedAlarms.entrySet().iterator();
        Gson gson = new Gson();
        Calendar cal = Calendar.getInstance();

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();

            TaskAlarm taskAlarm = gson.fromJson(pair.getValue().toString(), TaskAlarm.class);
            int _id = taskAlarm.getAlarmId();

            Intent intent = new Intent(context, TodoReceiver.class);
            intent.putExtra(TASK_NAME_INTENT_KEY, taskAlarm.getTaskText());
            intent.putExtra(TASK_ID_INTENT_KEY, taskAlarm.getTaskId());

            PendingIntent sender = PendingIntent.getBroadcast(context, _id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            cal.setTime(taskAlarm.getTaskDueDate());
            am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);

            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    private void saveAlarmsToPreferences(Map<String, String> inputMap) {
        SharedPreferences pSharedPref = context.getApplicationContext().getSharedPreferences(TASK_ALARMS_PREFERENCE_KEY, Context.MODE_PRIVATE);
        if (pSharedPref != null){
            JSONObject jsonObject = new JSONObject(inputMap);
            String jsonString = jsonObject.toString();
            SharedPreferences.Editor editor = pSharedPref.edit();
            editor.remove(TASK_ALARMS_KEY).commit();
            editor.putString(TASK_ALARMS_KEY, jsonString);
            editor.commit();
        }
    }

    private Map<String, String> loadAlarmsFromPreferences() {
        Map<String, String> outputMap = new HashMap<String, String>();
        SharedPreferences pSharedPref = context.getApplicationContext().getSharedPreferences(TASK_ALARMS_PREFERENCE_KEY, Context.MODE_PRIVATE);
        try{
            if (pSharedPref != null){
                String jsonString = pSharedPref.getString(TASK_ALARMS_KEY, (new JSONObject()).toString());
                JSONObject jsonObject = new JSONObject(jsonString);
                Iterator<String> keysItr = jsonObject.keys();
                while(keysItr.hasNext()) {
                    String key = keysItr.next();
                    String value = (String) jsonObject.get(key);
                    outputMap.put(key, value);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return outputMap;
    }
}
