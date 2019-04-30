package com.habitrpg.android.habitica.helpers

import android.content.Context
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.events.ShowCheckinDialog
import com.habitrpg.android.habitica.events.ShowSnackbarEvent
import com.habitrpg.android.habitica.models.Notification
import com.habitrpg.android.habitica.models.notifications.LoginIncentiveData
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.playseeds.android.sdk.inappmessaging.Log
import io.reactivex.functions.Consumer

import org.greenrobot.eventbus.EventBus

import java.util.HashMap

/**
 * Created by krh12 on 12/9/2016.
 */

class NotificationsManager (private val context: Context) {
    // @TODO: A queue for displaying alert dialogues

    private var seenNotifications: MutableMap<String, Boolean>? = null
    private var apiClient: ApiClient? = null

    private val notifications: BehaviorSubject<List<Notification>>

    init {
        this.seenNotifications = HashMap()
        this.notifications = BehaviorSubject.create()
    }

    fun setNotifications(current: List<Notification>) {
        this.notifications.onNext(current)
current.map { Log.d("NotificationsManager.setNotifications." + it.type) }

        this.showNotificationDialog(current)
    }

    fun getNotifications(): Flowable<List<Notification>> {
        return this.notifications.toFlowable(BackpressureStrategy.LATEST)
    }

    fun setApiClient(apiClient: ApiClient?) {
        this.apiClient = apiClient
    }

    fun displayNotification(notification: Notification): Boolean? {
        val notificationData = notification.data as LoginIncentiveData?
        val nextUnlockText = context.getString(R.string.nextPrizeUnlocks, notificationData!!.nextRewardAt)
        if (notificationData.rewardKey != null) {
            val event = ShowCheckinDialog()
            event.notification = notification
            event.nextUnlockText = nextUnlockText
            EventBus.getDefault().post(event)
        } else {
            val event = ShowSnackbarEvent()
            event.title = notificationData.message
            event.text = nextUnlockText
            event.type = HabiticaSnackbar.SnackbarDisplayType.BLUE
            EventBus.getDefault().post(event)
            if (apiClient != null) {
                // @TODO: This should be handled somewhere else? MAybe we notifiy via event
                apiClient!!.readNotification(notification.id)
                        .subscribe(Consumer {}, RxErrorHandler.handleEmptyError())
            }
        }
        return true
    }

    fun showNotificationDialog(notifications: List<Notification>?): Boolean? {
        if (notifications == null || notifications.size == 0) {
            return false
        }

        if (this.seenNotifications == null) {
            this.seenNotifications = HashMap()
        }

        for (notification in notifications) {
            if (this.seenNotifications!![notification.id] != null) {
                continue
            }

            if (notification.type != "LOGIN_INCENTIVE") {
                continue
            }

            this.displayNotification(notification)
            this.seenNotifications!![notification.id] = true
        }

        return true
    }
}
