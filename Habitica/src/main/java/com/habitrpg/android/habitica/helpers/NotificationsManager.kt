package com.habitrpg.android.habitica.helpers

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.common.habitica.models.Notification
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import java.lang.ref.WeakReference
import java.util.Date

interface NotificationsManager {
    val displayNotificationEvents: Flow<Notification>
    var apiClient: WeakReference<ApiClient>?

    fun setNotifications(current: List<Notification>)
    fun getNotifications(): Flow<List<Notification>>
    fun getNotification(id: String): Notification?
    fun dismissTaskNotification(context: Context, task: Task)
}

class MainNotificationsManager: NotificationsManager {

    private val seenNotifications: MutableMap<String, Boolean>
    override var apiClient: WeakReference<ApiClient>? = null

    private var lastNotificationHandling: Date? = null
    val _notifications = MutableStateFlow<List<Notification>?>(null)
    val _displaynotificationEvents = MutableStateFlow<Notification?>(null)
    override val displayNotificationEvents: Flow<Notification> = _displaynotificationEvents.filterNotNull()

    init {
        this.seenNotifications = HashMap()
    }

    override fun setNotifications(current: List<Notification>) {
        _notifications.value = current
        this.handlePopupNotifications(current)
    }

    override fun getNotifications(): Flow<List<Notification>> {
        return _notifications.filterNotNull()
    }

    override fun getNotification(id: String): Notification? {
        return _notifications.value?.find { it.id == id }
    }

    override fun dismissTaskNotification(context: Context, task: Task) {
        NotificationManagerCompat.from(context).cancel(task.id.hashCode())
    }

    private fun handlePopupNotifications(notifications: List<Notification>): Boolean {
        val now = Date()
        if (now.time - (lastNotificationHandling?.time ?: 0) < 300) {
            return true
        }
        lastNotificationHandling = now
        notifications
            .filter { !this.seenNotifications.containsKey(it.id) }
            .map {
                val notificationDisplayed = when (it.type) {
                    Notification.Type.ACHIEVEMENT_PARTY_UP.type -> true
                    Notification.Type.ACHIEVEMENT_PARTY_ON.type -> true
                    Notification.Type.ACHIEVEMENT_BEAST_MASTER.type -> true
                    Notification.Type.ACHIEVEMENT_MOUNT_MASTER.type -> true
                    Notification.Type.ACHIEVEMENT_TRIAD_BINGO.type -> true
                    Notification.Type.ACHIEVEMENT_GUILD_JOINED.type -> true
                    Notification.Type.ACHIEVEMENT_CHALLENGE_JOINED.type -> true
                    Notification.Type.ACHIEVEMENT_INVITED_FRIEND.type -> true

                    Notification.Type.ACHIEVEMENT_ALL_YOUR_BASE.type -> true
                    Notification.Type.ACHIEVEMENT_BACK_TO_BASICS.type -> true
                    Notification.Type.ACHIEVEMENT_JUST_ADD_WATER.type -> true
                    Notification.Type.ACHIEVEMENT_LOST_MASTERCLASSER.type -> true
                    Notification.Type.ACHIEVEMENT_MIND_OVER_MATTER.type -> true
                    Notification.Type.ACHIEVEMENT_DUST_DEVIL.type -> true
                    Notification.Type.ACHIEVEMENT_ARID_AUTHORITY.type -> true
                    Notification.Type.ACHIEVEMENT_MONSTER_MAGUS.type -> true
                    Notification.Type.ACHIEVEMENT_UNDEAD_UNDERTAKER.type -> true
                    Notification.Type.ACHIEVEMENT_PRIMED_FOR_PAINTING.type -> true
                    Notification.Type.ACHIEVEMENT_PEARLY_PRO.type -> true
                    Notification.Type.ACHIEVEMENT_TICKLED_PINK.type -> true
                    Notification.Type.ACHIEVEMENT_ROSY_OUTLOOK.type -> true
                    Notification.Type.ACHIEVEMENT_BUG_BONANZA.type -> true
                    Notification.Type.ACHIEVEMENT_BARE_NECESSITIES.type -> true
                    Notification.Type.ACHIEVEMENT_FRESHWATER_FRIENDS.type -> true
                    Notification.Type.ACHIEVEMENT_GOOD_AS_GOLD.type -> true
                    Notification.Type.ACHIEVEMENT_ALL_THAT_GLITTERS.type -> true
                    Notification.Type.ACHIEVEMENT_GOOD_AS_GOLD.type -> true
                    Notification.Type.ACHIEVEMENT_BONE_COLLECTOR.type -> true
                    Notification.Type.ACHIEVEMENT_SKELETON_CREW.type -> true
                    Notification.Type.ACHIEVEMENT_SEEING_RED.type -> true
                    Notification.Type.ACHIEVEMENT_RED_LETTER_DAY.type -> true

                    Notification.Type.ACHIEVEMENT_GENERIC.type -> true
                    Notification.Type.ACHIEVEMENT_ONBOARDING_COMPLETE.type -> true
                    Notification.Type.LOGIN_INCENTIVE.type -> true
                    else -> false
                }

                if (notificationDisplayed) {
                    _displaynotificationEvents.value = it
                    this.seenNotifications[it.id] = true
                    readNotification(it)
                }
            }

        return true
    }

    private fun readNotification(notification: Notification) {
        MainScope().launchCatching {
            apiClient?.get()?.readNotification(notification.id)
        }
    }
}
