package com.habitrpg.android.habitica.helpers.notifications;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.habitrpg.android.habitica.HabiticaApplication;

import javax.inject.Inject;

/**
 * Created by keithholliday on 6/24/16.
 */
public class HabiticaFirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Inject
    public PushNotificationManager pushNotificationManager;

    @Override
    public void onTokenRefresh() {
        HabiticaApplication.getComponent().inject(this);
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        pushNotificationManager.setRefreshedToken(refreshedToken);
    }

}
