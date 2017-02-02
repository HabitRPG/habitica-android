package com.habitrpg.android.habitica.helpers.notifications;

import com.habitrpg.android.habitica.ui.activities.MainActivity;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by keithholliday on 7/2/16.
 */
public class WonChallengeLocalNotification extends HabiticaLocalNotification {
    @Override
    public void notifyLocally(Context context, String title, String message) {
        super.notifyLocally(context, title, message);
        this.setNotificationActions();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.notify(10, notificationBuilder.build());
    }

    protected void setNotificationActions() {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                3000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        notificationBuilder.setContentIntent(pendingIntent);
    }
}
