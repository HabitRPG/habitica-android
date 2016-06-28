package com.habitrpg.android.habitica.helpers.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.HabiticaApplication;

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

    protected PushNotificationManager(Context context) {
        HabiticaApplication.getInstance(context).getComponent().inject(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PushNotificationManager getInstance(Context context) {
        if(instance == null) {
            instance = new PushNotificationManager(context);
        }
        return instance;
    }

    public void setRefreshedToken (String refreshedToken) {
        if (this.refreshedToken == null) {
            this.refreshedToken = refreshedToken;
//            sharedPreferences.put
        }
    }

    //@TODO: Use preferences
    public void addPushDeviceUsingStoredToken () {
        Map<String, String> pushDeviceData = new HashMap<String, String>();
        pushDeviceData.put("regIdRequired", this.refreshedToken);
        pushDeviceData.put("typeRequired", "android");
        apiHelper.apiService.addPushDevice(pushDeviceData);
    }

    public void removePushDeviceUsingStoredToken () {
        apiHelper.apiService.deletePushDevice(this.refreshedToken);
    }
}
