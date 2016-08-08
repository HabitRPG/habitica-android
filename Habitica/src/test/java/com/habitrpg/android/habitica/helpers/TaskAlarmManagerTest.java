package com.habitrpg.android.habitica.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.SystemUpdatePolicy;
import android.content.Context;
import android.content.Intent;

import android.test.mock.MockContext;
import android.util.Log;

import com.habitrpg.android.habitica.HabitDatabase;
import com.habitrpg.android.habitica.receivers.TaskReceiver;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Days;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.RemindersItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.raizlabs.android.dbflow.config.FlowManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by keithholliday on 7/16/16.
 */

@Config(manifest = "AndroidManifestTesting.xml")
@RunWith(value = RobolectricTestRunner.class)
public class TaskAlarmManagerTest {
    private TaskAlarmManager taskAlarmManager;

    private Context context;

    @Before
    public void setUp() {
        context = ShadowApplication.getInstance().getApplicationContext();
        taskAlarmManager = TaskAlarmManager.getInstance(context);
    }

    @After
    public void tearDown() {
        FlowManager.getDatabase(HabitDatabase.NAME).reset(context);
    }

    @Test
    public void testItSchedulesAlarmsForTodosWithMultipleReminders() {
        Task task = new Task();
        task.setType(Task.TYPE_TODO);

        List<RemindersItem> reminders = new ArrayList<RemindersItem>();
        RemindersItem remindersItem1 = new RemindersItem();
        UUID randomUUID = UUID.randomUUID();
        remindersItem1.setId(randomUUID.toString());

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + 1);

        remindersItem1.setTime(cal.getTime());
        reminders.add(remindersItem1);

        task.setReminders(reminders);
        task.save();

        taskAlarmManager.setAlarmsForTask(task);

        int intentId = remindersItem1.getId().hashCode() & 0xfffffff;
        Intent intent = new Intent(context, TaskReceiver.class);
        intent.setAction(remindersItem1.getId());
        PendingIntent sender = PendingIntent.getBroadcast(context, intentId, intent, PendingIntent.FLAG_NO_CREATE);
        boolean alarmUp = sender != null;

        Assert.assertNotNull(intentId);
        Assert.assertEquals(true, alarmUp);
    }

    @Test
    public void itUpdatesScheduledAlarmsForTodosWithMultipleReminders() {
        Task task = new Task();
        task.setType(Task.TYPE_TODO);

        List<RemindersItem> reminders = new ArrayList<RemindersItem>();
        RemindersItem remindersItem1 = new RemindersItem();
        UUID randomUUID = UUID.randomUUID();
        remindersItem1.setId(randomUUID.toString());

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + 1);

        remindersItem1.setTime(cal.getTime());
        reminders.add(remindersItem1);

        task.setReminders(reminders);
        task.save();

        taskAlarmManager.setAlarmsForTask(task);

        int alarmId = remindersItem1.getId().hashCode() & 0xfffffff;
        Intent intent = new Intent(context, TaskReceiver.class);
        intent.setAction(remindersItem1.getId());
        PendingIntent sender = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_NO_CREATE);
        boolean alarmUp = sender != null;

        Assert.assertNotNull(alarmId);
        Assert.assertEquals(true, alarmUp);


        reminders = new ArrayList<RemindersItem>();
        cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + 1);
        remindersItem1.setTime(cal.getTime());
        remindersItem1.save();
        reminders.add(remindersItem1);
        task.setReminders(reminders);
        task.save();

        taskAlarmManager.setAlarmsForTask(task);
        int newAlarmId = reminders.get(0).getId().hashCode() & 0xfffffff;
        PendingIntent senderNew = PendingIntent.getBroadcast(context, newAlarmId, intent, PendingIntent.FLAG_NO_CREATE);
        boolean alarmUpNew = senderNew != null;

        Assert.assertNotNull(newAlarmId);
        Assert.assertEquals(alarmId, newAlarmId);
        Assert.assertEquals(true, alarmUpNew);
    }

    @Test
    //@TODO: Pending
    public void itRemovesAlarmsWhenRemindersAreRemovedFromTodo() {
        Task task = new Task();
        task.setType(Task.TYPE_TODO);

        List<RemindersItem> reminders = new ArrayList<RemindersItem>();
        RemindersItem remindersItem1 = new RemindersItem();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + 1);
        remindersItem1.setTime(cal.getTime());
        reminders.add(remindersItem1);
        task.setReminders(reminders);
        task.save();

//        taskAlarmManager.setAlarmsForTask(task);
//
//        Integer alarmId = reminders.get(0).getAlarmId();
//        Intent intent = new Intent(context, TaskReceiver.class);
//        intent.setAction(remindersItem1.getAlarmId().toString());
//        PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//        try{ Thread.sleep(5000); }catch(InterruptedException e){ }
//        PendingIntent sender = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_NO_CREATE);
//        boolean alarmUp = sender != null;
//        System.out.println(sender);
//        Assert.assertNotNull(alarmId);
//        Assert.assertEquals(true, alarmUp);

//        remindersItem1.delete();
//
//        PendingIntent senderUpdated = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_NO_CREATE);
//        boolean alarmDown = senderUpdated == null;
//
//        Assert.assertEquals(true, alarmDown);
    }

    @Test
    //@TODO: Pending
    public void itRemovesAlarmsWhenTodoIsDeleted() {

    }

    @Test
    public void itScheduledAlarmForTheNextAvailableDayForRegularDaily() {
        Task task = new Task();
        task.setType(Task.TYPE_DAILY);
        task.setFrequency(Task.FREQUENCY_WEEKLY);

        Days taskRepeatDays = new Days();
        taskRepeatDays.setM(true);
        taskRepeatDays.setS(false);
        taskRepeatDays.setSu(false);
        task.setRepeat(taskRepeatDays);

        List<RemindersItem> reminders = new ArrayList<RemindersItem>();
        RemindersItem remindersItem1 = new RemindersItem();
        UUID randomUUID = UUID.randomUUID();
        remindersItem1.setId(randomUUID.toString());

        //We try to set a reminder on Tuesday, but the manager will correct this to Monday
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, 3);
        cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + 1);

        remindersItem1.setTime(cal.getTime());
        reminders.add(remindersItem1);

        task.setReminders(reminders);
        task.save();

        taskAlarmManager.setAlarmsForTask(task);

        int alarmId = reminders.get(0).getId().hashCode() & 0xfffffff;

        Calendar newReminderTime = Calendar.getInstance();
        newReminderTime.setTime(reminders.get(0).getTime());

        Intent intent = new Intent(context, TaskReceiver.class);
        intent.setAction(remindersItem1.getId());
        PendingIntent sender = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_NO_CREATE);
        boolean alarmUp = sender != null;

        Assert.assertNotNull(alarmId);
        Assert.assertEquals(true, alarmUp);
        Assert.assertEquals(1, newReminderTime.get(Calendar.DAY_OF_WEEK));
    }

    @Test
    //This also tests for when the receiver tries to schedule the next daily available
    public void itScheduledAlarmForTheNextAvailableDayForRegularDailyWhenUserTriesToSetAlarmForNow() {
        Task task = new Task();
        task.setType(Task.TYPE_DAILY);
        task.setFrequency(Task.FREQUENCY_WEEKLY);

        Days taskRepeatDays = new Days();
        taskRepeatDays.setM(true);
        taskRepeatDays.setT(true);
        taskRepeatDays.setW(true);
        taskRepeatDays.setTh(true);
        taskRepeatDays.setF(true);
        taskRepeatDays.setS(false);
        taskRepeatDays.setSu(false);
        task.setRepeat(taskRepeatDays);

        List<RemindersItem> reminders = new ArrayList<RemindersItem>();
        RemindersItem remindersItem1 = new RemindersItem();
        UUID randomUUID = UUID.randomUUID();
        remindersItem1.setId(randomUUID.toString());

        //We try to set a reminder for now, but by the manager will correct (because the seconds will be different)
        Calendar cal = Calendar.getInstance();

        remindersItem1.setTime(cal.getTime());
        reminders.add(remindersItem1);

        task.setReminders(reminders);
        task.save();

        taskAlarmManager.setAlarmsForTask(task);

        int alarmId = reminders.get(0).getId().hashCode() & 0xfffffff;

        Calendar newReminderTime = Calendar.getInstance();
        newReminderTime.setTime(reminders.get(0).getTime());

        Intent intent = new Intent(context, TaskReceiver.class);
        intent.setAction(remindersItem1.getId());
        PendingIntent sender = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_NO_CREATE);
        boolean alarmUp = sender != null;

        Assert.assertNotNull(alarmId);
        Assert.assertEquals(true, alarmUp);
        Assert.assertNotSame(cal.getTime(), newReminderTime);
    }

    @Test
    public void itScheduledAlarmForTheNextAvailableDayForEveryXDayDaily() {
        Task task = new Task();
        task.setType(Task.TYPE_DAILY);
        task.setFrequency(Task.FREQUENCY_DAILY);

        int everyXDay = 2;
        task.setEveryX(everyXDay);

        List<RemindersItem> reminders = new ArrayList<RemindersItem>();
        RemindersItem remindersItem1 = new RemindersItem();
        UUID randomUUID = UUID.randomUUID();
        remindersItem1.setId(randomUUID.toString());

        //We try to set a reminder one day after the start date, but the manager will correct since the
        // daily is every 2 days from above
        Calendar cal = Calendar.getInstance();
        int currentDayOfTheWeek = cal.get(Calendar.DAY_OF_WEEK);

        task.setStartDate(cal.getTime());

        cal.set(Calendar.DAY_OF_WEEK, currentDayOfTheWeek + 1);
        cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));

        remindersItem1.setTime(cal.getTime());
        reminders.add(remindersItem1);

        task.setReminders(reminders);
        task.save();

        taskAlarmManager.setAlarmsForTask(task);

        int alarmId = reminders.get(0).getId().hashCode() & 0xfffffff;

        Calendar newReminderTime = Calendar.getInstance();
        newReminderTime.setTime(reminders.get(0).getTime());

        Intent intent = new Intent(context, TaskReceiver.class);
        intent.setAction(remindersItem1.getId());
        PendingIntent sender = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_NO_CREATE);
        boolean alarmUp = sender != null;

        Assert.assertNotNull(alarmId);
        Assert.assertEquals(true, alarmUp);
        Assert.assertEquals(currentDayOfTheWeek + everyXDay, newReminderTime.get(Calendar.DAY_OF_WEEK));
    }

    @Test
    //This also tests for when the receiver tries to schedule the next daily available
    public void itScheduledAlarmForTheNextAvailableDayForEveryXDayDailyWhenUserTriesToSetAlarmForNow() {
        Task task = new Task();
        task.setType(Task.TYPE_DAILY);
        task.setFrequency(Task.FREQUENCY_DAILY);

        int everyXDay = 2;
        task.setEveryX(everyXDay);

        List<RemindersItem> reminders = new ArrayList<RemindersItem>();
        RemindersItem remindersItem1 = new RemindersItem();
        UUID randomUUID = UUID.randomUUID();
        remindersItem1.setId(randomUUID.toString());

        //We try to set a reminder for now, but the manager will correct since the seconds will be off
        Calendar cal = Calendar.getInstance();
        int currentDayOfTheWeek = cal.get(Calendar.DAY_OF_WEEK);

        remindersItem1.setTime(cal.getTime());
        reminders.add(remindersItem1);

        task.setReminders(reminders);
        task.save();

        taskAlarmManager.setAlarmsForTask(task);

        int alarmId = reminders.get(0).getId().hashCode() & 0xfffffff;

        Calendar newReminderTime = Calendar.getInstance();
        newReminderTime.setTime(reminders.get(0).getTime());

        Intent intent = new Intent(context, TaskReceiver.class);
        intent.setAction(remindersItem1.getId());
        PendingIntent sender = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_NO_CREATE);
        boolean alarmUp = sender != null;

        Assert.assertNotNull(alarmId);
        Assert.assertEquals(true, alarmUp);
        Assert.assertEquals(currentDayOfTheWeek + everyXDay, newReminderTime.get(Calendar.DAY_OF_WEEK));
    }
}
