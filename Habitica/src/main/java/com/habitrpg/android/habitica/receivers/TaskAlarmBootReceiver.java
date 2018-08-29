package com.habitrpg.android.habitica.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.helpers.TaskAlarmManager;

import java.util.Objects;

import javax.inject.Inject;

public class TaskAlarmBootReceiver extends BroadcastReceiver {

    @Inject
    TaskAlarmManager taskAlarmManager;
    @Inject
    SharedPreferences sharedPreferences;

    @Override
    public void onReceive(Context context, Intent arg1) {
        Objects.requireNonNull(HabiticaApplication.Companion.getComponent()).inject(this);
        taskAlarmManager.scheduleAllSavedAlarms(sharedPreferences.getBoolean("preventDailyReminder", false));
    }

}
