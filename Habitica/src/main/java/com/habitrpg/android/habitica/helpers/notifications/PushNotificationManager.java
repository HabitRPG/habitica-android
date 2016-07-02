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
    public static String PARTY_INVITE_PUSH_NOTIFICATION_KEY = "Invited To Party";
    public static String RECEIVED_PRIVATE_MESSAGE_PUSH_NOTIFICATION_KEY = "newPM";
    public static String RECEIVED_GEMS_PUSH_NOTIFICATION_KEY = "Gems";
    public static String RECEIVED_SUBSCRIPTION_GIFT_PUSH_NOTIFICATION_KEY = "Subscription";
    public static String GUILD_INVITE_PUSH_NOTIFICATION_KEY = "invitedGuild";
    public static String QUEST_INVITE_PUSH_NOTIFICATION_KEY = "questInvitation";
    public static String QUEST_BEGUN_PUSH_NOTIFICATION_KEY = "questStarted";
    public static String WON_CHALLENGE_PUSH_NOTIFICATION_KEY = "wonChallenge";


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

        if (this.userIsSubscribedToNotifications()) {
            this.addPushDeviceUsingStoredToken();
        }
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
        String remoteMessageIdentifier = remoteMessage.getData().get("identifier");

        HabiticaLocalNotificationFactory notificationFactory = new HabiticaLocalNotificationFactory();
        HabiticaLocalNotification notification = notificationFactory.build(remoteMessageIdentifier);
        if (userIsSubscribedToNotificationType(remoteMessageIdentifier) && notification != null) {
            notification.setExtras(remoteMessage.getData());
            notification.notifyLocally(this.context, remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }
    }

    private boolean userIsSubscribedToNotifications() {
        return sharedPreferences.getBoolean("pushNotifications", true);
    }

    private boolean userIsSubscribedToNotificationType(String type) {
        String key = "";

        //@TODO: If user has push turned off to send

        if (type.equals(PARTY_INVITE_PUSH_NOTIFICATION_KEY)) {
            key = "preference_push_invited_to_party";
        } else if (type.contains(RECEIVED_PRIVATE_MESSAGE_PUSH_NOTIFICATION_KEY)) {
            key = "preference_push_received_a_private_message";
        } else if (type.contains(RECEIVED_GEMS_PUSH_NOTIFICATION_KEY)) {
            key = "preference_push_gifted_gems";
        } else if (type.contains(RECEIVED_SUBSCRIPTION_GIFT_PUSH_NOTIFICATION_KEY)) {
            key = "preference_push_gifted_subscription";
        } else if (type.contains(GUILD_INVITE_PUSH_NOTIFICATION_KEY)) {
            key = "preference_push_invited_to_guild";
        } else if (type.contains(QUEST_INVITE_PUSH_NOTIFICATION_KEY)) {
            key = "preference_push_invited_to_quest";
        } else if (type.contains(QUEST_BEGUN_PUSH_NOTIFICATION_KEY)) {
            key = "preference_push_your_quest_has_begun";
        } else if (type.contains(WON_CHALLENGE_PUSH_NOTIFICATION_KEY)) {
            key = "preference_push_you_won_challenge";
        }

        return sharedPreferences.getBoolean(key, true);
    }
}
