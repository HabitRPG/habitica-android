package com.habitrpg.android.habitica.helpers.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.activities.PartyInviteActivity;

/**
 * Created by keithholliday on 6/28/16.
 */
public class PartyInviteLocalNotification implements HabiticaLocalNotification {

    public void notifyLocally(Context context, String title, String message) {
        Intent intent = new Intent(context, PartyInviteActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);

        Notification notification = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(message).setSmallIcon(R.drawable.ic_gryphon)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_action_delete_white_24, "Accept", pendingIntent)
                .addAction(R.drawable.ic_gryphon, "Reject", pendingIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify((int) System.currentTimeMillis(), notification);
    }

}
