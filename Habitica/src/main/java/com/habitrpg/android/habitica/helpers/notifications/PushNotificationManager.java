package com.habitrpg.android.habitica.helpers.notifications;

/**
 * Created by keithholliday on 6/27/16.
 */
public class PushNotificationManager {
    private static PushNotificationManager instance = null;

    protected PushNotificationManager() {
        // Exists only to defeat instantiation.
    }

    public static PushNotificationManager getInstance() {
        if(instance == null) {
            instance = new PushNotificationManager();
        }
        return instance;
    }
}
