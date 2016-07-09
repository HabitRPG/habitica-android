package com.habitrpg.android.habitica.helpers.notifications;

import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.CallSuper;
import android.support.v4.app.NotificationCompat;

import com.habitrpg.android.habitica.R;

import java.util.Map;

/**
 * Created by keithholliday on 6/28/16.
 */
public abstract class HabiticaLocalNotification {

    protected Map<String, String> data;
    protected Context context;
    protected String title;
    protected String message;

    protected NotificationCompat.Builder notificationBuilder;

    @CallSuper
    public void notifyLocally(Context context, String title, String message) {
        this.context = context;
        this.title = title;
        this.message = message;

        Uri path = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        this.notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_gryphon)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(path);
    }

    public void setExtras(Map<String, String> data) {
        this.data = data;
    }

    protected abstract void setNotificationActions();
}
