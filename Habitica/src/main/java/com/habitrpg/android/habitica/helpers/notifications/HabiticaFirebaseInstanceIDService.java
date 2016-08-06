package com.habitrpg.android.habitica.helpers.notifications;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.habitrpg.android.habitica.APIHelper;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

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
