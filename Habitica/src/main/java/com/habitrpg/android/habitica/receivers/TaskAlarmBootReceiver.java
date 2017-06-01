package com.habitrpg.android.habitica.receivers;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.helpers.TaskAlarmManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

public class TaskAlarmBootReceiver extends BroadcastReceiver {

    @Inject
    TaskAlarmManager taskAlarmManager;

    @Override
    public void onReceive(Context context, Intent arg1) {
        HabiticaApplication.getComponent().inject(this);
        taskAlarmManager.scheduleAllSavedAlarms();
    }

}
