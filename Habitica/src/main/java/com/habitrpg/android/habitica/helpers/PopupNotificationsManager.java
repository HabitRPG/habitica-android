package com.habitrpg.android.habitica.helpers;

import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.magicmicky.habitrpgwrapper.lib.api.IApiClient;
import com.magicmicky.habitrpgwrapper.lib.models.Notification;
import com.magicmicky.habitrpgwrapper.lib.models.notifications.Reward;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by krh12 on 12/9/2016.
 */

public class PopupNotificationsManager {
    private static PopupNotificationsManager instance;
    private Map<String, Boolean> seenNotifications;
    private IApiClient apiClient;
    private Context context;

    // @TODO: A queue for displaying alert dialogues

    private PopupNotificationsManager(IApiClient apiClient, Context context) {
        this.apiClient = apiClient;
        this.seenNotifications = new HashMap<>();
        this.context = context.getApplicationContext();
    }

    public static PopupNotificationsManager getInstance(IApiClient apiHelper, Context context) {
        if (instance == null) {
            instance = new PopupNotificationsManager(apiHelper, context);
        }
        return instance;
    }

    public Boolean displayNotification(Notification notification) {
        String title = notification.data.message;
        String youEarnedMessage = "";

        LayoutInflater factory = LayoutInflater.from(HabiticaApplication.currentActivity);
        final View view = factory.inflate(R.layout.dialog_login_incentive, null);

        SimpleDraweeView imageView = (SimpleDraweeView) view.findViewById(R.id.imageView);
        String imageKey = "inventory_present_11";
        if (notification.data.rewardKey != null) {
            imageKey = notification.data.rewardKey.get(0);

            if (notification.data.reward != null && notification.data.reward.size() > 0) {
                String earnedString = "";
                int count = 0;
                for (Reward reward : notification.data.reward) {
                    earnedString += reward.key;
                    count += 1;
                    if (notification.data.reward.size() > 1 && count != notification.data.reward.size()) {
                        earnedString += ", ";
                    }
                }
                youEarnedMessage = context.getString(R.string.checkInRewardEarned, earnedString);
            }
        }
        DataBindingUtils.loadImage(imageView, imageKey);

        TextView youEarnedTexView = (TextView) view.findViewById(R.id.you_earned_message);
        youEarnedTexView.setText(youEarnedMessage);

        String message = context.getString(R.string.nextPrizeUnlocks, notification.data.nextRewardAt);
        TextView nextUnlockTextView = (TextView) view.findViewById(R.id.next_unlock_message);
        nextUnlockTextView.setText(message);

        Button confirmButton = (Button) view.findViewById(R.id.confirm_button);

        AlertDialog.Builder builder = new AlertDialog.Builder(HabiticaApplication.currentActivity)
                .setTitle(title)
                .setView(view)
                .setMessage("");

        final AlertDialog dialog = builder.create();
        dialog.show();

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (apiClient != null) {
                    // @TODO: This should be handled somewhere else? MAybe we notifiy via event
                    apiClient.readNotificaiton(notification.getId())
                            .subscribe(next -> {}, throwable -> {});
                }

                dialog.hide();
            }
        });

        return true;
    }

    public Boolean showNotificationDialog(final List<Notification> notifications) {
        if (notifications == null || notifications.size() == 0) {
            return false;
        }

        if (HabiticaApplication.currentActivity == null || HabiticaApplication.currentActivity.isFinishing()) {
            return false;
        }

        HabiticaApplication.currentActivity.runOnUiThread(() -> {
            if (HabiticaApplication.currentActivity == null) return;
            if ((HabiticaApplication.currentActivity).isFinishing()) return;

            if (this.seenNotifications == null) {
                this.seenNotifications = new HashMap<>();
            }

            for (Notification notification : notifications) {
                if (this.seenNotifications.get(notification.getId()) != null) {
                    continue;
                }

                if (!notification.getType().equals("LOGIN_INCENTIVE")) {
                    continue;
                }

                this.displayNotification(notification);
                this.seenNotifications.put(notification.getId(), true);
            }
        });

        return true;
    }
}
