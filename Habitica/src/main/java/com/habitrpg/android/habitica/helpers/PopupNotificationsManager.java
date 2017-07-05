package com.habitrpg.android.habitica.helpers;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.Space;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.repacked.kotlin.Unit;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.events.ShowSnackbarEvent;
import com.habitrpg.android.habitica.models.Notification;
import com.habitrpg.android.habitica.models.notifications.Reward;
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils;
import com.habitrpg.android.habitica.ui.helpers.UiUtils;
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar;
import com.mikepenz.aboutlibraries.util.UIUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by krh12 on 12/9/2016.
 */

public class PopupNotificationsManager {
    private Map<String, Boolean> seenNotifications;
    @Nullable
    private ApiClient apiClient;
    private Context context;

    // @TODO: A queue for displaying alert dialogues

    public PopupNotificationsManager(Context context) {
        this.seenNotifications = new HashMap<>();
        this.context = context;
    }

    public void setApiClient(@Nullable ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    Boolean displayNotification(Notification notification) {
        if (notification.data.rewardKey != null) {
            String title = notification.data.message;

            LayoutInflater factory = LayoutInflater.from(context);
            final View view = factory.inflate(R.layout.dialog_login_incentive, null);

            SimpleDraweeView imageView = (SimpleDraweeView) view.findViewById(R.id.imageView);
            String imageKey = notification.data.rewardKey.get(0);
            DataBindingUtils.loadImage(imageView, imageKey);

            String youEarnedMessage = context.getString(R.string.checkInRewardEarned, notification.data.rewardText)

            TextView titleTextView = new TextView(context);
            titleTextView.setBackgroundResource(R.color.best_100);
            titleTextView.setTextColor(ContextCompat.getColor(context, R.color.white));
            float density = context.getResources().getDisplayMetrics().density;
            int paddingDp = (int) (16 * density);
            titleTextView.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);
            titleTextView.setTextSize(18);
            titleTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            titleTextView.setText(title);

            TextView youEarnedTexView = (TextView) view.findViewById(R.id.you_earned_message);
            youEarnedTexView.setText(youEarnedMessage);

            String message = context.getString(R.string.nextPrizeUnlocks, notification.data.nextRewardAt);
            TextView nextUnlockTextView = (TextView) view.findViewById(R.id.next_unlock_message);
            nextUnlockTextView.setText(message);

            AlertDialog.Builder builder = new AlertDialog.Builder(HabiticaApplication.currentActivity, R.style.AlertDialogTheme)
                    .setView(view)
                    .setCustomTitle(titleTextView)
                    .setPositiveButton(R.string.start_day, (dialog, which) -> {
                        if (apiClient != null) {
                            // @TODO: This should be handled somewhere else? MAybe we notifiy via event
                            apiClient.readNotificaiton(notification.getId())
                                    .subscribe(next -> {}, RxErrorHandler.handleEmptyError());
                        }
                    })
                    .setMessage("");

            final AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            ShowSnackbarEvent event = new ShowSnackbarEvent();
            event.title = notification.data.message;
            event.text = context.getString(R.string.nextPrizeUnlocks, notification.data.nextRewardAt);
            event.type = HabiticaSnackbar.SnackbarDisplayType.BLUE;
            EventBus.getDefault().post(event);
            if (apiClient != null) {
                // @TODO: This should be handled somewhere else? MAybe we notifiy via event
                apiClient.readNotificaiton(notification.getId())
                        .subscribe(next -> {
                        }, RxErrorHandler.handleEmptyError());
            }
        }
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
