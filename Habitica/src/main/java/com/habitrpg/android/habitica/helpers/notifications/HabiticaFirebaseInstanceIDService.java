package com.habitrpg.android.habitica.helpers.notifications;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.habitrpg.android.habitica.HabiticaApplication;

import java.util.Objects;

import javax.inject.Inject;

/**
 * Created by keithholliday on 6/24/16.
 */
public class HabiticaFirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Inject
    public PushNotificationManager pushNotificationManager;

    @Override
    public void onTokenRefresh() {
        Objects.requireNonNull(HabiticaApplication.Companion.getComponent()).inject(this);
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        if (refreshedToken != null) {
            pushNotificationManager.setRefreshedToken(refreshedToken);
        }
    }

}
