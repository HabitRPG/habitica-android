package com.habitrpg.android.habitica.helpers.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.RemoteMessage;
import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.models.HabitRPGUser;
import com.habitrpg.android.habitica.models.PushDevice;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class PushNotificationManager {

    static final String PARTY_INVITE_PUSH_NOTIFICATION_KEY = "invitedParty";
    static final String RECEIVED_PRIVATE_MESSAGE_PUSH_NOTIFICATION_KEY = "newPM";
    static final String RECEIVED_GEMS_PUSH_NOTIFICATION_KEY = "giftedGems";
    static final String RECEIVED_SUBSCRIPTION_GIFT_PUSH_NOTIFICATION_KEY = "giftedSubscription";
    static final String GUILD_INVITE_PUSH_NOTIFICATION_KEY = "invitedGuild";
    static final String QUEST_INVITE_PUSH_NOTIFICATION_KEY = "questInvitation";
    static final String QUEST_BEGUN_PUSH_NOTIFICATION_KEY = "questStarted";
    static final String WON_CHALLENGE_PUSH_NOTIFICATION_KEY = "wonChallenge";
    private static final String DEVICE_TOKEN_PREFERENCE_KEY = "device-token-preference";
    private static PushNotificationManager instance = null;
    @Inject
    public ApiClient apiClient;

    private String refreshedToken;
    private SharedPreferences sharedPreferences;
    private Context context;
    private HabitRPGUser user;

    protected PushNotificationManager(Context context) {
        HabiticaBaseApplication.getComponent().inject(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PushNotificationManager getInstance(Context context) {
        if (instance == null) {
            instance = new PushNotificationManager(context.getApplicationContext());
        }

        instance.refreshedToken = instance.sharedPreferences.getString(DEVICE_TOKEN_PREFERENCE_KEY, "");
        instance.context = context.getApplicationContext();

        return instance;
    }

    public void setUser(HabitRPGUser user) {
        this.user = user;
    }

    void setRefreshedToken(String refreshedToken) {
        if (this.refreshedToken == null) {
            return;
        }

        this.refreshedToken = refreshedToken;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DEVICE_TOKEN_PREFERENCE_KEY, refreshedToken);
        editor.apply();
    }

    //@TODO: Use preferences
    public void addPushDeviceUsingStoredToken() {
        if (this.refreshedToken == null || this.refreshedToken.isEmpty()) {
            this.refreshedToken = FirebaseInstanceId.getInstance().getToken();
        }

        if (this.refreshedToken == null || this.refreshedToken.isEmpty()) {
            return;
        }

        if (this.user == null || this.userHasPushDevice()) {
            return;
        }

        if (!this.userIsSubscribedToNotifications()) {
            return;
        }

        Map<String, String> pushDeviceData = new HashMap<>();
        pushDeviceData.put("regId", this.refreshedToken);
        pushDeviceData.put("type", "android");
        apiClient.addPushDevice(pushDeviceData)

            .subscribe(aVoid -> {}, throwable -> {});
    }

    public void removePushDeviceUsingStoredToken() {
        apiClient.deletePushDevice(this.refreshedToken)

            .subscribe(aVoid -> {}, throwable -> {});
    }

    private Boolean userHasPushDevice() {
        if (this.user.getPushDevices() == null) {
            return true;
        }

        for (PushDevice pushDevice : this.user.getPushDevices()) {
            if (pushDevice.getRegId().equals(this.refreshedToken)) {
                return true;
            }
        }
        return false;
    }

    void displayNotification(RemoteMessage remoteMessage) {
        String remoteMessageIdentifier = remoteMessage.getData().get("identifier");

        HabiticaLocalNotificationFactory notificationFactory = new HabiticaLocalNotificationFactory();
        HabiticaLocalNotification notification = notificationFactory.build(remoteMessageIdentifier);
        if (userIsSubscribedToNotificationType(remoteMessageIdentifier) && notification != null) {
            notification.setExtras(remoteMessage.getData());
            notification.notifyLocally(this.context, remoteMessage.getData().get("title"), remoteMessage.getData().get("body"));
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
