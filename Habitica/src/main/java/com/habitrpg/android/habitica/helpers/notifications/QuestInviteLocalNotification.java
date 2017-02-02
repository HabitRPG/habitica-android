package com.habitrpg.android.habitica.helpers.notifications;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.receivers.LocalNotificationActionReceiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

/**
 * Created by keithholliday on 7/1/16.
 */
public class QuestInviteLocalNotification extends HabiticaLocalNotification {
    @Override
    public void notifyLocally(Context context, String title, String message) {
        super.notifyLocally(context, title, message);
        this.setNotificationActions();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(10000, notificationBuilder.build());
    }

    protected void setNotificationActions() {
        Resources res = context.getResources();

        Intent acceptInviteIntent = new Intent(context, LocalNotificationActionReceiver.class);
        acceptInviteIntent.setAction(res.getString(R.string.accept_quest_invite));
        PendingIntent pendingIntentAccept = PendingIntent.getBroadcast(
                context,
                3000,
                acceptInviteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        notificationBuilder.addAction(0, "Accept", pendingIntentAccept);

        Intent rejectInviteIntent = new Intent(context, LocalNotificationActionReceiver.class);
        rejectInviteIntent.setAction(res.getString(R.string.reject_quest_invite));
        PendingIntent pendingIntentReject = PendingIntent.getBroadcast(
                context,
                2000,
                rejectInviteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        notificationBuilder.addAction(0, "Reject", pendingIntentReject);
    }
}
