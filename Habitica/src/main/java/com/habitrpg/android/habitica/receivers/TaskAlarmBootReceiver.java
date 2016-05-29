package com.habitrpg.android.habitica.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.habitrpg.android.habitica.ui.helpers.TaskAlarmManager;

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
