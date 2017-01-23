package com.habitrpg.android.habitica.helpers;

import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.magicmicky.habitrpgwrapper.lib.models.Notification;
import com.magicmicky.habitrpgwrapper.lib.models.notifications.Reward;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by krh12 on 12/9/2016.
 */

public class PopupNotificationsManager {
    private Map<String, Boolean> seenNotifications;
    private APIHelper apiHelper;
    private static PopupNotificationsManager instance;

    // @TODO: A queue for displaying alert dialogues

    private PopupNotificationsManager(APIHelper apiHelper) {
        this.apiHelper = apiHelper;
        this.seenNotifications = new HashMap<>();
    }

    public static PopupNotificationsManager getInstance(APIHelper apiHelper) {
        if (instance == null) {
            instance = new PopupNotificationsManager(apiHelper);
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
                youEarnedMessage = "You earned a " + earnedString + " as a reward for your devotion to improving your life.";
            }
        }
        DataBindingUtils.loadImage(imageView, imageKey);

        String message = "Your next prize unlocks at " + notification.data.nextRewardAt + " Check-Ins";
        TextView nextUnlockTextView = (TextView) view.findViewById(R.id.next_unlock_message);
        nextUnlockTextView.setText(message);

        TextView youEarnedTexView = (TextView) view.findViewById(R.id.you_earned_message);
        youEarnedTexView.setText(youEarnedMessage);

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
                if (apiHelper != null) {
                    // @TODO: This should be handled somewhere else? MAybe we notifiy via event
                    apiHelper.apiService.readNotificaiton(notification.getId())
                            .compose(apiHelper.configureApiCallObserver())
                            .subscribe(next -> {
                            }, throwable -> {
                            });
                }

                dialog.hide();
            }
        });

        return true;
    }

    public Boolean showNotificationDialog(final List<Notification> notifications) {
        if (notifications.size() == 0) {
            return false;
        }

        HabiticaApplication.currentActivity.runOnUiThread(() -> {
            if (HabiticaApplication.currentActivity == null) return;
            if ((HabiticaApplication.currentActivity).isFinishing()) return;

            if (this.seenNotifications == null) {
                this.seenNotifications = new HashMap<>();
            }

            for (Notification notification: notifications) {
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
