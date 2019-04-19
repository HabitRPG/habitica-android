package com.habitrpg.android.habitica.ui.viewmodels

import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.notifications.GlobalNotification
import com.habitrpg.android.habitica.models.notifications.NewChatMessageData
import com.habitrpg.android.habitica.models.notifications.NotificationType
import com.habitrpg.android.habitica.models.social.UserParty
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.realm.RealmList
import java.util.HashMap

open class NotificationsViewModel : BaseViewModel() {
    var party: UserParty? = null

    override fun inject(component: AppComponent) {
        component.inject(this)
    }

    fun getNotifications(): Flowable<List<GlobalNotification>> {
        return userRepository.getUser()
                .doOnEach { party = it.value?.party }
                .map { filterSupportedTypes(it.notifications) }
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getNotificationCount(): Flowable<Int> {
        return getNotifications()
                .map { it.count() }
                .distinctUntilChanged()
    }

    fun allNotificationsSeen(): Flowable<Boolean> {
        return getNotifications()
                .map { it.all { notification -> notification.seen == true } }
                .distinctUntilChanged()
    }

    fun refreshNotifications(): Flowable<RealmList<GlobalNotification>> {
        return userRepository.retrieveUser(withTasks = false, forced = true).map { it.notifications }
    }

    private fun filterSupportedTypes(notifications: List<GlobalNotification>): List<GlobalNotification> {
        return notifications.filter { NotificationType.contains(it.type) }
    }

    fun isPartyMessage(data: NewChatMessageData?): Boolean {
        if (party == null || data?.group?.id == null) {
            return false
        }

        return party?.id == data.group?.id
    }

    fun dismissNotification(notification: GlobalNotification) {
        disposable.add(userRepository.readNotification(notification.id)
                .subscribe(Consumer {
                    // TODO better way to handle updates than reload whole user ??
                    refreshNotifications()
                }, RxErrorHandler.handleEmptyError()))
    }

    fun dismissAllNotifications(notifications: List<GlobalNotification>) {
        if (notifications.isEmpty()) {
            return
        }

        val notificationIds = HashMap<String, List<String>>()
        notificationIds["notificationIds"] = notifications.map { notification -> notification.id }

        disposable.add(userRepository.readNotifications(notificationIds)
                .subscribe(Consumer {
                    refreshNotifications()
                }, RxErrorHandler.handleEmptyError()))
    }

    fun markNotificationsAsSeen(notifications: List<GlobalNotification>) {
        val unseenIds = notifications.filter { notification -> notification.seen != true }
                .map { notification -> notification.id }

        if (unseenIds.isEmpty()) {
            return
        }

        val notificationIds = HashMap<String, List<String>>()
        notificationIds["notificationIds"] = unseenIds

        disposable.add(userRepository.seeNotifications(notificationIds)
                .subscribe(Consumer {
                    refreshNotifications()
                }, RxErrorHandler.handleEmptyError()))
    }

}
