package com.habitrpg.android.habitica.receivers;

import com.habitrpg.android.habitica.helpers.TaskAlarmManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TaskAlarmBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context arg0, Intent arg1) {
        TaskAlarmManager taskAlarmManager = TaskAlarmManager.getInstance(arg0);
        taskAlarmManager.scheduleAllSavedAlarms();
    }

}
