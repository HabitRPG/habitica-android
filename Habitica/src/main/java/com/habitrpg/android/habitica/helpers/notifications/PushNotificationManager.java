package com.habitrpg.android.habitica.helpers.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.RemoteMessage;
import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.callbacks.HabitRPGUserCallback;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by keithholliday on 6/27/16.
 */
public class PushNotificationManager {

    private static PushNotificationManager instance = null;
    private static String DEVICE_TOKEN_PREFERENCE_STRING = "device-token-preference-string";

    @Inject
    public APIHelper apiHelper;

    private String refreshedToken;
    private SharedPreferences sharedPreferences;
    private Context context;

    protected PushNotificationManager(Context context) {
        HabiticaApplication.getInstance(context).getComponent().inject(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PushNotificationManager getInstance(Context context) {
        if(instance == null) {
            instance = new PushNotificationManager(context);
        }

        instance.context = context;

        return instance;
    }

    public void setRefreshedToken (String refreshedToken) {
        if (this.refreshedToken == null) {
            return;
        }
        this.refreshedToken = refreshedToken;
//            sharedPreferences.put
    }

    //@TODO: Use preferences
    public void addPushDeviceUsingStoredToken () {
        if (this.refreshedToken == null) {
            this.refreshedToken = FirebaseInstanceId.getInstance().getToken();
        }

        if (this.refreshedToken == null) {
            return;
        }

        Map<String, String> pushDeviceData = new HashMap<String, String>();
        pushDeviceData.put("regId", this.refreshedToken);
        pushDeviceData.put("type", "android");
        apiHelper.apiService.addPushDevice(pushDeviceData)
            .compose(apiHelper.configureApiCallObserver())
            .subscribe(aVoid -> {}, throwable -> {});
    }

    public void removePushDeviceUsingStoredToken () {
        apiHelper.apiService.deletePushDevice(this.refreshedToken)
            .compose(apiHelper.configureApiCallObserver())
            .subscribe(aVoid -> {}, throwable -> {});
    }

    public void displayNotification (RemoteMessage remoteMessage) {
        HabiticaLocalNotificationFactory notificationFactory = new HabiticaLocalNotificationFactory();
        HabiticaLocalNotification notification = notificationFactory.build("PARTY_INVITE_NOTIFICATION");
        if (userIsSubscribedToNotificationType("PARTY_INVITE_NOTIFICATION") && notification != null) {
            notification.notifyLocally(this.context, remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }
    }

    private boolean userIsSubscribedToNotificationType(String type) {
        String key = "";

        if (type.equals("PARTY_INVITE_NOTIFICATION")) {
            key = "preference_push_invited_to_party";
        }

        return sharedPreferences.getBoolean(key, true);
    }
}
