package com.habitrpg.android.habitica.helpers.notifications;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by keithholliday on 6/24/16.
 */
public class HabiticaFirebaseInstanceIDService extends FirebaseInstanceIdService {

    public PushNotificationManager pushNotificationManager;

    @Override
    public void onTokenRefresh() {
        pushNotificationManager = PushNotificationManager.getInstance(this);
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        pushNotificationManager.setRefreshedToken(refreshedToken);
    }

}
