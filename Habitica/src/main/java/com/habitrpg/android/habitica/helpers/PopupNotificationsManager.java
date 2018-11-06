package com.habitrpg.android.habitica.helpers;

import android.content.Context;
import androidx.annotation.Nullable;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.events.ShowCheckinDialog;
import com.habitrpg.android.habitica.events.ShowSnackbarEvent;
import com.habitrpg.android.habitica.models.Notification;
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar;

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
        String nextUnlockText = context.getString(R.string.nextPrizeUnlocks, notification.data.nextRewardAt);
        if (notification.data.rewardKey != null) {
            ShowCheckinDialog event = new ShowCheckinDialog();
            event.notification = notification;
            event.nextUnlockText = nextUnlockText;
            EventBus.getDefault().post(event);
        } else {
            ShowSnackbarEvent event = new ShowSnackbarEvent();
            event.title = notification.data.message;
            event.text = nextUnlockText;
            event.type = HabiticaSnackbar.SnackbarDisplayType.BLUE;
            EventBus.getDefault().post(event);
            if (apiClient != null) {
                // @TODO: This should be handled somewhere else? MAybe we notifiy via event
                apiClient.readNotification(notification.getId())
                        .subscribe(next -> {}, RxErrorHandler.handleEmptyError());
            }
        }
        return true;
    }

    public Boolean showNotificationDialog(final List<Notification> notifications) {
        if (notifications == null || notifications.size() == 0) {
            return false;
        }

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

        return true;
    }
}
