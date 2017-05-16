package com.habitrpg.android.habitica;

import com.habitrpg.android.habitica.helpers.TaskAlarmManager;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.List;


//https://gist.github.com/BrandonSmith/6679223
public class NotificationPublisher extends BroadcastReceiver {

    public static String NOTIFICATION_ID = "notification-id";
    public static String CHECK_DAILIES = "check-dailies";
    private Context context;

    public void onReceive(Context context, Intent intent) {
        Log.d("NotificationPublisher", "Publishing notification");

        this.context = context;

        boolean check_dailies = intent.getBooleanExtra(CHECK_DAILIES, false);
        Notification notification = getNotification();
        boolean show_notification = true;
        if (check_dailies) {
            List<Task> dailies = new Select().from(Task.class)
                    .where(Condition.column("type").eq("daily"))
                    .and(Condition.column("completed").eq(false))
                    .queryList();
            show_notification = false;
            for (Task task : dailies) {
                if (task.checkIfDue(0)) {
                    show_notification = true;
                    break;
                }
            }
            TaskAlarmManager.scheduleDailyReminder(context);
        }
        if (show_notification) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            int id = intent.getIntExtra(NOTIFICATION_ID, 0);
            notificationManager.notify(id, notification);
        }
    }

    @TargetApi(21)
    private Notification getNotification() {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;

        Notification notification;
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(context.getString(R.string.app_name));
        builder.setContentText(context.getString(R.string.reminder_title));
        builder.setSmallIcon(R.drawable.ic_gryphon_white);
        Intent notificationIntent = new Intent(context, MainActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent intent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        builder.setContentIntent(intent);

        if (currentapiVersion >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(ContextCompat.getColor(context, R.color.brand_300));
        }

        if (currentapiVersion >= Build.VERSION_CODES.JELLY_BEAN) {
            notification = builder.build();
        } else {
            notification = builder.getNotification();
        }
        notification.defaults |= Notification.DEFAULT_LIGHTS;

        notification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
        return notification;
    }
}