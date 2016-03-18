package com.habitrpg.android.habitica;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.habitrpg.android.habitica.ui.fragments.PreferencesFragment;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            boolean use_reminder = preferences.getBoolean("use_reminder", false);
            String reminder_time = preferences.getString("reminder_time", "19:00");
            if (use_reminder) {
                PreferencesFragment.scheduleNotifications(context, reminder_time);
            }
        }
    }
}
