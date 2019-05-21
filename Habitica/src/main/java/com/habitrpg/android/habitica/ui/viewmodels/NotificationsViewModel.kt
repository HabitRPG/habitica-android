package com.habitrpg.android.habitica.ui.viewmodels

import android.os.Bundle
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.NotificationsManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.Notification
import com.habitrpg.android.habitica.models.notifications.GuildInvitationData
import com.habitrpg.android.habitica.models.notifications.NewChatMessageData
import com.habitrpg.android.habitica.models.notifications.PartyInvitationData
import com.habitrpg.android.habitica.models.notifications.QuestInvitationData
import com.habitrpg.android.habitica.models.social.UserParty
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import java.util.*
import javax.inject.Inject

open class NotificationsViewModel : BaseViewModel() {
    @Inject
    lateinit var notificationsManager: NotificationsManager

    /**
     * A list of notification types handled by this component.
     * NOTE: Those not listed here won't be shown in the notification panel (except the custom ones)
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
     * A list of notification types that are "actionable" (ones that have accept/reject buttons).
     */
    private val actionableNotificationTypes = listOf(
            Notification.Type.GUILD_INVITATION.type,
            Notification.Type.PARTY_INVITATION.type,
            Notification.Type.QUEST_INVITATION.type
    )

    /**
     * Keep track of users party so we can determine which chat notifications are party chat
     * instead of guild chat notifications.
     */
    private var party: UserParty? = null

    /**
     * Custom notification types created by this class (from user data).
     * Will be added to the notifications coming from server.
     */
    private var customNotifications: List<Notification> = emptyList()

    override fun inject(component: UserComponent) {
        component.inject(this)
    }

    init {
        disposable.add(userRepository.getUser()
                .subscribe(Consumer {
                    party = it.party
                    customNotifications = convertInvitationsToNotifications(it)
                }, RxErrorHandler.handleEmptyError()))
    }


    fun getNotifications(): Flowable<List<Notification>> {
        return notificationsManager.getNotifications()
                .map { filterSupportedTypes(it) }
                .map { it.plus(customNotifications) }
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getNotificationCount(): Flowable<Int> {
        return getNotifications()
                .map { it.count() }
                .distinctUntilChanged()
    }

    fun allNotificationsSeen(): Flowable<Boolean> {
        return getNotifications()
                .map { it.all { notification -> notification.seen != false } }
                .distinctUntilChanged()
    }

    fun getHasPartyNotification(): Flowable<Boolean> {
        return getNotifications()
                .map {
                    it.find { notification ->
                        val data = notification.data as? NewChatMessageData
                        isPartyMessage(data)
                    } != null
                }
                .distinctUntilChanged()
    }

    fun refreshNotifications(): Flowable<*> {
        return userRepository.retrieveUser(withTasks = false, forced = true)
    }

    private fun filterSupportedTypes(notifications: List<Notification>): List<Notification> {
        return notifications.filter { supportedNotificationTypes.contains(it.type) }
    }

    private fun convertInvitationsToNotifications(user: User): List<Notification> {
        val notifications = arrayListOf<Notification>()

        notifications.addAll(user.invitations?.parties?.map {
            val notification = Notification()
            notification.id = "custom-party-invitation-" + it.id
            notification.type = Notification.Type.PARTY_INVITATION.type
            val data = PartyInvitationData()
            data.invitation = it
            notification.data = data
            notification
        } ?: emptyList())

        notifications.addAll(user.invitations?.getGuilds()?.map {
            val notification = Notification()
            notification.id = "custom-guild-invitation-" + it.id
            notification.type = Notification.Type.GUILD_INVITATION.type
            val data = GuildInvitationData()
            data.invitation = it
            notification.data = data
            notification
        } ?: emptyList())

        val quest = user.party?.quest
        if (quest != null && quest.RSVPNeeded) {
            val notification = Notification()
            notification.id = "custom-quest-invitation-" + user.party?.id
            notification.type = Notification.Type.QUEST_INVITATION.type
            val data = QuestInvitationData()
            data.questKey = quest.key
            notification.data = data

            notifications.add(notification)
        }

        return notifications
    }

    fun isPartyMessage(data: NewChatMessageData?): Boolean {
        if (party == null || data?.group?.id == null) {
            return false
        }

        return party?.id == data.group?.id
    }

    /**
     * Is the given notification an "artificial" custom notification (created by this class)
     * instead of one of the ones coming from server.
     */
    private fun isCustomNotification(notification: Notification): Boolean {
        return notification.id.startsWith("custom-")
    }

    fun dismissNotification(notification: Notification) {
        if (isCustomNotification(notification)) {
            return
        }

        disposable.add(userRepository.readNotification(notification.id)
                .subscribe(Consumer {}, RxErrorHandler.handleEmptyError()))
    }

    fun dismissAllNotifications(notifications: List<Notification>) {
        val dismissableIds = notifications
                .filter { !isCustomNotification(it) }
                .filter { !actionableNotificationTypes.contains(it.type) }
                .map { it.id }

        if (dismissableIds.isEmpty()) {
            return
        }

        val notificationIds = HashMap<String, List<String>>()
        notificationIds["notificationIds"] = dismissableIds

        disposable.add(userRepository.readNotifications(notificationIds)
                .subscribe(Consumer {}, RxErrorHandler.handleEmptyError()))
    }

    fun markNotificationsAsSeen(notifications: List<Notification>) {
        val unseenIds = notifications
                .filter { !isCustomNotification(it) }
                .filter { it.seen == false }
                .map { it.id }

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
            // Group tasks should go to Group tasks view if that is added to this app at some point
            Notification.Type.GROUP_TASK_APPROVED.type -> navController.navigate(R.id.tasksFragment)
            Notification.Type.GROUP_TASK_NEEDS_WORK.type -> navController.navigate(R.id.tasksFragment)
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
