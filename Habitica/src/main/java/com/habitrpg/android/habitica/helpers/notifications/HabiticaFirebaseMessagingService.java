package com.habitrpg.android.habitica.helpers.notifications;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by keithholliday on 6/24/16.
 */
public class HabiticaFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        Log.d("test", "From: " + remoteMessage.getFrom());
        Log.d("test", "Notification Message Body: " + remoteMessage.getNotification().getBody());
    }
}
