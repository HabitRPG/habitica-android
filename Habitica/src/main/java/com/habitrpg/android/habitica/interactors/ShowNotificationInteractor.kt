package com.habitrpg.android.habitica.interactors

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.Analytics
import com.habitrpg.android.habitica.helpers.AnalyticsTarget
import com.habitrpg.android.habitica.helpers.EventCategory
import com.habitrpg.android.habitica.helpers.HitType
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.SnackbarActivity
import com.habitrpg.android.habitica.ui.views.dialogs.AchievementDialog
import com.habitrpg.android.habitica.ui.views.dialogs.FirstDropDialog
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.dialogs.WonChallengeDialog
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.models.Notification
import com.habitrpg.common.habitica.models.notifications.AchievementData
import com.habitrpg.common.habitica.models.notifications.ChallengeWonData
import com.habitrpg.common.habitica.models.notifications.FirstDropData
import com.habitrpg.common.habitica.models.notifications.LoginIncentiveData
import com.habitrpg.common.habitica.views.PixelArtView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ShowNotificationInteractor(
    private val activity: Activity,
    private val lifecycleScope: LifecycleCoroutineScope
) {
    fun handleNotification(notification: Notification): Boolean {
        when (notification.type) {
            Notification.Type.LOGIN_INCENTIVE.type -> showCheckinDialog(notification)
            Notification.Type.ACHIEVEMENT_PARTY_UP.type -> showAchievementDialog(notification)
            Notification.Type.ACHIEVEMENT_PARTY_ON.type -> showAchievementDialog(notification)
            Notification.Type.ACHIEVEMENT_BEAST_MASTER.type -> showAchievementDialog(notification)
            Notification.Type.ACHIEVEMENT_MOUNT_MASTER.type -> showAchievementDialog(notification)
            Notification.Type.ACHIEVEMENT_TRIAD_BINGO.type -> showAchievementDialog(notification)
            Notification.Type.ACHIEVEMENT_GUILD_JOINED.type -> showAchievementDialog(notification)
            Notification.Type.ACHIEVEMENT_CHALLENGE_JOINED.type ->
                showAchievementDialog(
                    notification
                )

            Notification.Type.ACHIEVEMENT_INVITED_FRIEND.type -> showAchievementDialog(notification)
            Notification.Type.WON_CHALLENGE.type -> showWonChallengeDialog(notification)

            Notification.Type.ACHIEVEMENT_ALL_YOUR_BASE.type -> showAchievementDialog(notification)
            Notification.Type.ACHIEVEMENT_BACK_TO_BASICS.type -> showAchievementDialog(notification)
            Notification.Type.ACHIEVEMENT_JUST_ADD_WATER.type -> showAchievementDialog(notification)
            Notification.Type.ACHIEVEMENT_LOST_MASTERCLASSER.type ->
                showAchievementDialog(
                    notification
                )

            Notification.Type.ACHIEVEMENT_MIND_OVER_MATTER.type ->
                showAchievementDialog(
                    notification
                )

            Notification.Type.ACHIEVEMENT_DUST_DEVIL.type -> showAchievementDialog(notification)
            Notification.Type.ACHIEVEMENT_ARID_AUTHORITY.type -> showAchievementDialog(notification)
            Notification.Type.ACHIEVEMENT_MONSTER_MAGUS.type -> showAchievementDialog(notification)
            Notification.Type.ACHIEVEMENT_UNDEAD_UNDERTAKER.type ->
                showAchievementDialog(
                    notification
                )

            Notification.Type.ACHIEVEMENT_PRIMED_FOR_PAINTING.type ->
                showAchievementDialog(
                    notification
                )

            Notification.Type.ACHIEVEMENT_PEARLY_PRO.type -> showAchievementDialog(notification)
            Notification.Type.ACHIEVEMENT_TICKLED_PINK.type -> showAchievementDialog(notification)
            Notification.Type.ACHIEVEMENT_ROSY_OUTLOOK.type -> showAchievementDialog(notification)
            Notification.Type.ACHIEVEMENT_BUG_BONANZA.type -> showAchievementDialog(notification)
            Notification.Type.ACHIEVEMENT_BARE_NECESSITIES.type ->
                showAchievementDialog(
                    notification
                )

            Notification.Type.ACHIEVEMENT_FRESHWATER_FRIENDS.type ->
                showAchievementDialog(
                    notification
                )

            Notification.Type.ACHIEVEMENT_GOOD_AS_GOLD.type -> showAchievementDialog(notification)
            Notification.Type.ACHIEVEMENT_ALL_THAT_GLITTERS.type ->
                showAchievementDialog(
                    notification
                )

            Notification.Type.ACHIEVEMENT_GOOD_AS_GOLD.type -> showAchievementDialog(notification)
            Notification.Type.ACHIEVEMENT_BONE_COLLECTOR.type -> showAchievementDialog(notification)
            Notification.Type.ACHIEVEMENT_SKELETON_CREW.type -> showAchievementDialog(notification)
            Notification.Type.ACHIEVEMENT_SEEING_RED.type -> showAchievementDialog(notification)
            Notification.Type.ACHIEVEMENT_RED_LETTER_DAY.type -> showAchievementDialog(notification)

            Notification.Type.ACHIEVEMENT_GENERIC.type -> showAchievementDialog(notification)
            Notification.Type.ACHIEVEMENT_ONBOARDING_COMPLETE.type ->
                showAchievementDialog(
                    notification
                )

            Notification.Type.FIRST_DROP.type -> showFirstDropDialog(notification)
            else -> return false
        }
        return true
    }

    fun showCheckinDialog(notification: Notification) {
        val notificationData = notification.data as? LoginIncentiveData
        val nextUnlockText =
            activity.getString(R.string.nextPrizeUnlocks, notificationData?.nextRewardAt)
        if (notificationData?.rewardKey != null) {
            val title = notificationData.message

            val factory = LayoutInflater.from(activity)
            val view = factory.inflate(R.layout.dialog_login_incentive, null)

            val imageView = view.findViewById(R.id.imageView) as? PixelArtView
            var imageKey = notificationData.rewardKey?.get(0)
            if (imageKey?.contains("armor") == true) {
                imageKey = "slim_$imageKey"
            }
            imageView?.loadImage(imageKey)

            val youEarnedMessage =
                activity.getString(R.string.checkInRewardEarned, notificationData.rewardText)
            val youEarnedTexView = view.findViewById(R.id.you_earned_message) as? TextView
            youEarnedTexView?.text = youEarnedMessage

            val nextUnlockTextView = view.findViewById(R.id.next_unlock_message) as? TextView
            if ((notificationData.nextRewardAt ?: 0) > 0) {
                nextUnlockTextView?.text = nextUnlockText
            } else {
                nextUnlockTextView?.visibility = View.GONE
            }

            lifecycleScope.launch(context = Dispatchers.Main) {
                if (activity.isFinishing) return@launch
                val alert = HabiticaAlertDialog(activity)
                alert.setAdditionalContentView(view)
                alert.setTitle(title)
                alert.addButton(R.string.see_you_tomorrow, true)
                alert.show()
            }
        } else {
            (activity as? SnackbarActivity)?.showSnackbar(
                title = notificationData?.message,
                content = nextUnlockText,
                displayType = HabiticaSnackbar.SnackbarDisplayType.BLUE
            )
        }
    }

    fun showAchievementDialog(notification: Notification) {
        val data = (notification.data as? AchievementData) ?: return
        val achievement = data.achievement ?: notification.type ?: ""
        val delayTime: Long =
            if (achievement == "createdTask" || achievement == Notification.Type.ACHIEVEMENT_ONBOARDING_COMPLETE.type) {
                1000
            } else {
                200
            }
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            delay(delayTime)
            lifecycleScope.launch(context = Dispatchers.Main) {
                val dialog = AchievementDialog(activity)
                dialog.isLastOnboardingAchievement = data.isLastOnboardingAchievement
                dialog.setType(data.achievement ?: "", data.message, data.modalText)
                dialog.enqueue()
            }
        }
        logOnboardingEvents(achievement)
    }

    private fun showFirstDropDialog(notification: Notification) {
        val data = notification.data as? FirstDropData ?: return
        lifecycleScope.launch(context = Dispatchers.Main) {
            val dialog = FirstDropDialog(activity)
            dialog.configure(data.egg ?: "", data.hatchingPotion ?: "")
            dialog.enqueue()
        }
    }

    private fun showWonChallengeDialog(notification: Notification) {
        lifecycleScope.launch(context = Dispatchers.Main) {
            val dialog = WonChallengeDialog(activity)
            dialog.configure(notification.data as? ChallengeWonData)
            dialog.enqueue()
        }
    }

    private fun logOnboardingEvents(type: String) {
        if (User.ONBOARDING_ACHIEVEMENT_KEYS.contains(type) || type == Notification.Type.ACHIEVEMENT_ONBOARDING_COMPLETE.type) {
            Analytics.sendEvent(type, EventCategory.BEHAVIOUR, HitType.EVENT, null, AnalyticsTarget.FIREBASE)
        }
    }
}
