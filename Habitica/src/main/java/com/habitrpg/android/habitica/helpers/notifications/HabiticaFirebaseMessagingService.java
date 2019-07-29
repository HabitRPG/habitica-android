package com.habitrpg.android.habitica.helpers.notifications;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.habitrpg.android.habitica.HabiticaApplication;

import java.util.Objects;

import javax.inject.Inject;

/**
 * Created by keithholliday on 6/24/16.
 */
public class HabiticaFirebaseMessagingService extends FirebaseMessagingService {

    @Inject
    public PushNotificationManager pushNotificationManager;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Objects.requireNonNull(HabiticaApplication.Companion.getUserComponent()).inject(this);
        pushNotificationManager.displayNotification(remoteMessage);
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Objects.requireNonNull(HabiticaApplication.Companion.getUserComponent()).inject(this);
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        if (refreshedToken != null) {
            pushNotificationManager.setRefreshedToken(refreshedToken);
        }
    }
}
