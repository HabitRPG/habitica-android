package com.habitrpg.android.habitica.receivers;

import com.habitrpg.android.habitica.ui.helpers.TaskAlarmManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by keithholliday on 5/29/16.
 */
public class TaskAlarmBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context arg0, Intent arg1) {
        TaskAlarmManager taskAlarmManager = TaskAlarmManager.getInstance(arg0);
        taskAlarmManager.scheduleAllSavedAlarms();
    }

}
