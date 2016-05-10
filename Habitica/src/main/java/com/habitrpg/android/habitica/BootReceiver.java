package com.habitrpg.android.habitica;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Boot receiver so that we can restore alarms when the phone boots.
 *
 * Keep in mind that the BootReceiver does not work for applications
 * installed on external storage
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        scheduleReminder(context);
    }

    private void scheduleReminder(Context context) {
        NotificationPublisher.scheduleNotifications(context);
    }
}