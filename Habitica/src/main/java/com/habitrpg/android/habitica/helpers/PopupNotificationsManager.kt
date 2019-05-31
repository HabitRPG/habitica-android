package com.habitrpg.android.habitica.helpers

import android.content.Context
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.events.ShowCheckinDialog
import com.habitrpg.android.habitica.events.ShowSnackbarEvent
import com.habitrpg.android.habitica.models.Notification
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import io.reactivex.functions.Consumer
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 * Created by krh12 on 12/9/2016.
 */

class PopupNotificationsManager(private val context: Context) {
    private var seenNotifications: MutableMap<String, Boolean>? = null
    private var apiClient: ApiClient? = null

    init {
        this.seenNotifications = HashMap()
    }

    fun setApiClient(apiClient: ApiClient?) {
        this.apiClient = apiClient
    }

    private fun displayNotification(notification: Notification): Boolean? {
        val nextUnlockText = context.getString(R.string.nextPrizeUnlocks, notification.data.nextRewardAt)
        if (notification.data.rewardKey != null) {
            val event = ShowCheckinDialog()
            event.notification = notification
            event.nextUnlockText = nextUnlockText
            EventBus.getDefault().post(event)
        } else {
            val event = ShowSnackbarEvent()
            event.title = notification.data.message
            event.text = nextUnlockText
            event.type = HabiticaSnackbar.SnackbarDisplayType.BLUE
            EventBus.getDefault().post(event)
            apiClient?.readNotification(notification.id)?.subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
        }
        return true
    }

    fun showNotificationDialog(notifications: List<Notification>?): Boolean? {
        if (notifications == null || notifications.isEmpty()) {
            return false
        }

        if (this.seenNotifications == null) {
            this.seenNotifications = HashMap()
        }

        for (notification in notifications) {
            if (this.seenNotifications?.get(notification.id) != null) {
                continue
            }

            if (notification.type != "LOGIN_INCENTIVE") {
                continue
            }

            this.displayNotification(notification)
            this.seenNotifications?.put(notification.id, true)
        }

        return true
    }
}
