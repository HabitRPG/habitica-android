package com.habitrpg.android.habitica.helpers.notifications;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.habitrpg.android.habitica.HabiticaApplication;

import javax.inject.Inject;

/**
 * Created by keithholliday on 6/24/16.
 */
public class HabiticaFirebaseMessagingService extends FirebaseMessagingService {

    @Inject
    public PushNotificationManager pushNotificationManager;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        HabiticaApplication.getComponent().inject(this);
        pushNotificationManager.displayNotification(remoteMessage);
    }
}
