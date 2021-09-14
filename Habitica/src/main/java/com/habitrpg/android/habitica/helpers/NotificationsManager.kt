package com.habitrpg.android.habitica.helpers

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.events.*
import com.habitrpg.android.habitica.models.Notification
import com.habitrpg.android.habitica.models.notifications.AchievementData
import com.habitrpg.android.habitica.models.notifications.ChallengeWonData
import com.habitrpg.android.habitica.models.notifications.FirstDropData
import com.habitrpg.android.habitica.models.notifications.LoginIncentiveData
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationsManager(private val context: Context) {
    private val seenNotifications: MutableMap<String, Boolean>
    private var apiClient: ApiClient? = null

    private val notifications: BehaviorSubject<List<Notification>>

    private var lastNotificationHandling: Date? = null

    init {
        this.seenNotifications = HashMap()
        this.notifications = BehaviorSubject.create()
    }

    fun setNotifications(current: List<Notification>) {
        this.notifications.onNext(current)

        this.handlePopupNotifications(current)
    }

    fun getNotifications(): Flowable<List<Notification>> {
        return this.notifications.startWithArray(emptyList())
            .toFlowable(BackpressureStrategy.LATEST)
    }

    fun getNotification(id: String): Notification? {
        return this.notifications.value?.find { it.id == id }
    }

    fun setApiClient(apiClient: ApiClient?) {
        this.apiClient = apiClient
    }

    private fun handlePopupNotifications(notifications: List<Notification>): Boolean? {
        val now = Date()
        if (now.time - (lastNotificationHandling?.time ?: 0) < 300) {
            return true
        }
        lastNotificationHandling = now
        notifications
            .filter { !this.seenNotifications.containsKey(it.id) }
            .map {
                val notificationDisplayed = when (it.type) {
                    Notification.Type.LOGIN_INCENTIVE.type -> displayLoginIncentiveNotification(it)
                    Notification.Type.ACHIEVEMENT_PARTY_UP.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_PARTY_ON.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_BEAST_MASTER.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_MOUNT_MASTER.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_TRIAD_BINGO.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_GUILD_JOINED.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_CHALLENGE_JOINED.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_INVITED_FRIEND.type -> displayAchievementNotification(it)
                    Notification.Type.WON_CHALLENGE.type -> displayWonChallengeNotificaiton(it)

                    Notification.Type.ACHIEVEMENT_ALL_YOUR_BASE.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_BACK_TO_BASICS.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_JUST_ADD_WATER.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_LOST_MASTERCLASSER.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_MIND_OVER_MATTER.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_DUST_DEVIL.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_ARID_AUTHORITY.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_MONSTER_MAGUS.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_UNDEAD_UNDERTAKER.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_PRIMED_FOR_PAINTING.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_PEARLY_PRO.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_TICKLED_PINK.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_ROSY_OUTLOOK.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_BUG_BONANZA.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_BARE_NECESSITIES.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_FRESHWATER_FRIENDS.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_GOOD_AS_GOLD.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_ALL_THAT_GLITTERS.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_GOOD_AS_GOLD.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_BONE_COLLECTOR.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_SKELETON_CREW.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_SEEING_RED.type -> displayAchievementNotification(it)
                    Notification.Type.ACHIEVEMENT_RED_LETTER_DAY.type -> displayAchievementNotification(it)

                    Notification.Type.ACHIEVEMENT_GENERIC.type -> displayAchievementNotification(
                        it,
                        notifications.find { notif ->
                            notif.type == Notification.Type.ACHIEVEMENT_ONBOARDING_COMPLETE.type
                        } != null
                    )
                    Notification.Type.ACHIEVEMENT_ONBOARDING_COMPLETE.type -> displayAchievementNotification(it)
                    Notification.Type.FIRST_DROP.type -> displayFirstDropNotification(it)
                    else -> false
                }

                if (notificationDisplayed == true) {
                    this.seenNotifications[it.id] = true
                }
            }

        return true
    }

    private fun displayWonChallengeNotificaiton(notification: Notification): Boolean {
        EventBus.getDefault().post(ShowWonChallengeDialog(notification.id, notification.data as? ChallengeWonData))
        return true
    }

    private fun displayFirstDropNotification(notification: Notification): Boolean {
        val data = (notification.data as? FirstDropData)
        EventBus.getDefault().post(ShowFirstDropDialog(data?.egg ?: "", data?.hatchingPotion ?: "", notification.id))
        return true
    }

    private fun displayLoginIncentiveNotification(notification: Notification): Boolean? {
        val notificationData = notification.data as? LoginIncentiveData
        val nextUnlockText = context.getString(R.string.nextPrizeUnlocks, notificationData?.nextRewardAt)
        if (notificationData?.rewardKey != null) {
            val event = ShowCheckinDialog(notification, nextUnlockText, notificationData.nextRewardAt ?: 0)
            EventBus.getDefault().post(event)
        } else {
            val event = ShowSnackbarEvent()
            event.title = notificationData?.message
            event.text = nextUnlockText
            event.type = HabiticaSnackbar.SnackbarDisplayType.BLUE
            EventBus.getDefault().post(event)
            if (apiClient != null) {
                apiClient?.readNotification(notification.id)
                    ?.subscribe({}, RxErrorHandler.handleEmptyError())
            }
        }
        return true
    }

    private fun displayAchievementNotification(notification: Notification, isLastOnboardingAchievement: Boolean = false): Boolean {
        val data = (notification.data as? AchievementData)
        val achievement = data?.achievement ?: notification.type ?: ""
        val delay: Long = if (achievement == "createdTask" || achievement == Notification.Type.ACHIEVEMENT_ONBOARDING_COMPLETE.type) {
            1000
        } else {
            200
        }
        val sub = Completable.complete()
            .delay(delay, TimeUnit.MILLISECONDS)
            .subscribe(
                {
                    EventBus.getDefault().post(ShowAchievementDialog(achievement, notification.id, data?.message, data?.modalText, isLastOnboardingAchievement))
                },
                RxErrorHandler.handleEmptyError()
            )
        logOnboardingEvents(achievement)
        return true
    }

    private fun logOnboardingEvents(type: String) {
        if (User.ONBOARDING_ACHIEVEMENT_KEYS.contains(type)) {
            FirebaseAnalytics.getInstance(context).logEvent(type, null)
        } else if (type == Notification.Type.ACHIEVEMENT_ONBOARDING_COMPLETE.type) {
            FirebaseAnalytics.getInstance(context).logEvent(type, null)
        }
    }
}
