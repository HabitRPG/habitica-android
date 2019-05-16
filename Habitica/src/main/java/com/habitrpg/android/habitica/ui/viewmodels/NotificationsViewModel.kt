package com.habitrpg.android.habitica.ui.viewmodels

import android.os.Bundle
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.NotificationsManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.Notification
import com.habitrpg.android.habitica.models.notifications.NewChatMessageData
import com.habitrpg.android.habitica.models.social.UserParty
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import java.util.HashMap
import javax.inject.Inject

open class NotificationsViewModel : BaseViewModel() {
    @Inject
    lateinit var notificationsManager: NotificationsManager

    /**
     * A list of notification types handled by this component.
     * NOTE: Those not listed here won't be shown in the notification panel
     */
    private val supportedNotificationTypes = listOf(
            Notification.Type.NEW_STUFF.type,
            Notification.Type.NEW_CHAT_MESSAGE.type,
            Notification.Type.NEW_MYSTERY_ITEMS.type,
            Notification.Type.GROUP_TASK_NEEDS_WORK.type,
            Notification.Type.GROUP_TASK_APPROVED.type,
            Notification.Type.UNALLOCATED_STATS_POINTS.type
    )

    /**
     * Keep track of users party so we can determine which chat notifications are party chat
     * instead of guild chat notifications.
     */
    private var party: UserParty? = null

    override fun inject(component: AppComponent) {
        component.inject(this)
    }

    init {
        disposable.add(userRepository.getUser()
                .subscribe(Consumer {
                    party = it.party
                }, RxErrorHandler.handleEmptyError()))
    }


    fun getNotifications(): Flowable<List<Notification>> {
        return notificationsManager.getNotifications()
                .map { filterSupportedTypes(it) }
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

    fun refreshNotifications(): Flowable<*> {
        return userRepository.retrieveUser(withTasks = false, forced = true)
    }

    private fun filterSupportedTypes(notifications: List<Notification>): List<Notification> {
        return notifications.filter { supportedNotificationTypes.contains(it.type) }
    }

    fun isPartyMessage(data: NewChatMessageData?): Boolean {
        if (party == null || data?.group?.id == null) {
            return false
        }

        return party?.id == data.group?.id
    }

    fun dismissNotification(notification: Notification) {
        disposable.add(userRepository.readNotification(notification.id)
                .subscribe(Consumer {}, RxErrorHandler.handleEmptyError()))
    }

    fun dismissAllNotifications(notifications: List<Notification>) {
        if (notifications.isEmpty()) {
            return
        }

        val notificationIds = HashMap<String, List<String>>()
        notificationIds["notificationIds"] = notifications.map { notification -> notification.id }

        disposable.add(userRepository.readNotifications(notificationIds)
                .subscribe(Consumer {}, RxErrorHandler.handleEmptyError()))
    }

    fun markNotificationsAsSeen(notifications: List<Notification>) {
        val unseenIds = notifications.filter { notification -> notification.seen != true }
                .map { notification -> notification.id }

        if (unseenIds.isEmpty()) {
            return
        }

        val notificationIds = HashMap<String, List<String>>()
        notificationIds["notificationIds"] = unseenIds

        disposable.add(userRepository.seeNotifications(notificationIds)
                .subscribe(Consumer {}, RxErrorHandler.handleEmptyError()))
    }

    fun click(notificationId: String, navController: MainNavigationController) {
        val notification = notificationsManager.getNotification(notificationId) ?: return

        dismissNotification(notification)

        when (notification.type) {
            Notification.Type.NEW_STUFF.type -> navController.navigate(R.id.newsFragment)
            Notification.Type.NEW_CHAT_MESSAGE.type -> clickNewChatMessage(notification, navController)
            Notification.Type.NEW_MYSTERY_ITEMS.type -> navController.navigate(R.id.itemsFragment)
            Notification.Type.UNALLOCATED_STATS_POINTS.type -> navController.navigate(R.id.statsFragment)
        }
    }

    private fun clickNewChatMessage(notification: Notification, navController: MainNavigationController) {
        val data = notification.data as? NewChatMessageData
        if (isPartyMessage(data)) {
            navController.navigate(R.id.partyFragment)
        } else {
            val bundle = Bundle()
            bundle.putString("groupID", data?.group?.id)
            bundle.putBoolean("isMember", true) // safe to assume user is member since they got the notification
            navController.navigate(R.id.guildFragment, bundle)
        }
    }

}
