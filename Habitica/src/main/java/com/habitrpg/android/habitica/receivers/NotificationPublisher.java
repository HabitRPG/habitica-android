package com.habitrpg.android.habitica.receivers;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.helpers.TaskAlarmManager;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.modules.AppModule;
import com.habitrpg.android.habitica.ui.activities.MainActivity;

import javax.inject.Inject;
import javax.inject.Named;


//https://gist.github.com/BrandonSmith/6679223
public class NotificationPublisher extends WakefulBroadcastReceiver {

    @Inject
    TaskRepository taskRepository;
    @Inject
    @Named(AppModule.NAMED_USER_ID)
    String userId;

    public static String NOTIFICATION_ID = "notification-id";
    public static String CHECK_DAILIES = "check-dailies";
    private Context context;

    public void onReceive(Context context, Intent intent) {
        this.context = context;
        if (taskRepository == null) {
            HabiticaApplication.getComponent().inject(this);
        }

        boolean check_dailies = intent.getBooleanExtra(CHECK_DAILIES, false);
        Notification notification = getNotification();
        if (check_dailies) {
            taskRepository.getTasks(Task.TYPE_DAILY, userId).subscribe(dailies -> {
                boolean showNotifications = false;
                for (Task task : dailies) {
                    if (task.checkIfDue(0)) {
                        showNotifications = true;
                        break;
                    }
                }
                TaskAlarmManager.scheduleDailyReminder(context);
                if (showNotifications) {
                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    int id = intent.getIntExtra(NOTIFICATION_ID, 0);
                    notificationManager.notify(id, notification);
                }
            }, RxErrorHandler.handleEmptyError());

        } else {
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
