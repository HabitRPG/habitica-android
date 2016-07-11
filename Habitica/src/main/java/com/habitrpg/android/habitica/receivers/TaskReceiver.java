package com.habitrpg.android.habitica.receivers;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.helpers.TaskAlarmManager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

/**
 * Created by keithholliday on 5/29/16.
 */
public class TaskReceiver extends BroadcastReceiver {

    private Context context;

    private String taskId;
    private String taskTitle;
    private TaskAlarmManager taskAlarmManager;

    @Override
    public void onReceive(Context arg0, Intent arg1) {
        context = arg0;
        taskAlarmManager = TaskAlarmManager.getInstance(context);

        Bundle extras = arg1.getExtras();

        if (extras != null) {
            taskTitle = extras.getString(TaskAlarmManager.TASK_NAME_INTENT_KEY);
            taskId = extras.getString(TaskAlarmManager.TASK_ID_INTENT_KEY);
            //This will set up the next reminders for dailies
            taskAlarmManager.addAlarmForTaskId(taskId);
            createNotification();
        }
    }

    public void createNotification() {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_gryphon)
                .setContentTitle(taskTitle)
                .setContentText(taskTitle)
                .setSound(soundUri)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
    }
}