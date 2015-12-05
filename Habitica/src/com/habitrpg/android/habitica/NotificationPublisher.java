package com.habitrpg.android.habitica;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.List;


//https://gist.github.com/BrandonSmith/6679223
public class NotificationPublisher extends BroadcastReceiver {

    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";
    public static String CHECK_DAILIES = "check-dailies";

    public void onReceive(Context context, Intent intent) {
        Log.d("NotificationPublisher", "Publishing notification");
        boolean check_dailies = intent.getBooleanExtra(CHECK_DAILIES, false);
        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        boolean show_notification = true;
        if (check_dailies) {
            List<Task> dailies = new Select().from(Task.class)
                    .where(Condition.column("type").eq("daily"))
                    .and(Condition.column("completed").eq(false))
                    .queryList();
            show_notification = false;
            for (Task task : dailies) {
                if (task.isDue(0)) {
                    show_notification = true;
                    break;
                }
            }
        }
        if (show_notification) {
            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

            int id = intent.getIntExtra(NOTIFICATION_ID, 0);
            notificationManager.notify(id, notification);
        }
    }
}