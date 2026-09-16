package com.habitrpg.android.habitica.interactors

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.SnackbarActivity
import com.habitrpg.android.habitica.ui.views.dialogs.AchievementDialog
import com.habitrpg.android.habitica.ui.views.dialogs.FirstDropDialog
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.dialogs.RebirthAchievementDialog
import com.habitrpg.android.habitica.ui.views.dialogs.RebirthEnabledDialog
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
import kotlinx.coroutines.launch

class ShowNotificationInteractor(
    private val activity: Activity,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val userRepository: UserRepository,
) {
    fun handleNotification(notification: Notification): Boolean {
        when (notification.type) {
            Notification.Type.LOGIN_INCENTIVE.type -> {
                showCheckinDialog(notification)
            }

            Notification.Type.WON_CHALLENGE.type -> {
                showWonChallengeDialog(notification)
            }

            Notification.Type.ACHIEVEMENT_PARTY_UP.type,
            Notification.Type.ACHIEVEMENT_PARTY_ON.type,
            Notification.Type.ACHIEVEMENT_BEAST_MASTER.type,
            Notification.Type.ACHIEVEMENT_MOUNT_MASTER.type,
            Notification.Type.ACHIEVEMENT_TRIAD_BINGO.type,
            Notification.Type.ACHIEVEMENT_GUILD_JOINED.type,
            Notification.Type.ACHIEVEMENT_CHALLENGE_JOINED.type,
            Notification.Type.ACHIEVEMENT_INVITED_FRIEND.type,
            Notification.Type.ACHIEVEMENT_ALL_YOUR_BASE.type,
            Notification.Type.ACHIEVEMENT_BACK_TO_BASICS.type,
            Notification.Type.ACHIEVEMENT_JUST_ADD_WATER.type,
            Notification.Type.ACHIEVEMENT_LOST_MASTERCLASSER.type,
            Notification.Type.ACHIEVEMENT_MIND_OVER_MATTER.type,
            Notification.Type.ACHIEVEMENT_DUST_DEVIL.type,
            Notification.Type.ACHIEVEMENT_ARID_AUTHORITY.type,
            Notification.Type.ACHIEVEMENT_UNDEAD_UNDERTAKER.type,
            Notification.Type.ACHIEVEMENT_MONSTER_MAGUS.type,
            Notification.Type.ACHIEVEMENT_PRIMED_FOR_PAINTING.type,
            Notification.Type.ACHIEVEMENT_PEARLY_PRO.type,
            Notification.Type.ACHIEVEMENT_TICKLED_PINK.type,
            Notification.Type.ACHIEVEMENT_ROSY_OUTLOOK.type,
            Notification.Type.ACHIEVEMENT_BUG_BONANZA.type,
            Notification.Type.ACHIEVEMENT_BARE_NECESSITIES.type,
            Notification.Type.ACHIEVEMENT_FRESHWATER_FRIENDS.type,
            Notification.Type.ACHIEVEMENT_GOOD_AS_GOLD.type,
            Notification.Type.ACHIEVEMENT_ALL_THAT_GLITTERS.type,
            Notification.Type.ACHIEVEMENT_GOOD_AS_GOLD.type,
            Notification.Type.ACHIEVEMENT_BONE_COLLECTOR.type,
            Notification.Type.ACHIEVEMENT_SKELETON_CREW.type,
            Notification.Type.ACHIEVEMENT_SEEING_RED.type,
            Notification.Type.ACHIEVEMENT_RED_LETTER_DAY.type,
            Notification.Type.ACHIEVEMENT_ULTIMATE_GEAR.type,
            Notification.Type.ACHIEVEMENT_GENERIC.type,
            -> {
                showAchievementDialog(notification)
            }

            Notification.Type.ACHIEVEMENT_ONBOARDING_COMPLETE.type -> {
                showOnboardingCompletedDialog(notification)
            }

            Notification.Type.REBIRTH_ENABLED.type -> {
                showRebirthEnabledDialog()
            }

            Notification.Type.REBIRTH_ACHIEVEMENT.type -> {
                showRebirthAchievementDialog()
            }

            Notification.Type.FIRST_DROP.type -> {
                showFirstDropDialog(notification)
            }

            else -> {
                return notification.type?.contains("ACHIEVEMENT") == true
            }
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
                displayType = HabiticaSnackbar.SnackbarDisplayType.BLUE,
            )
        }
    }

    fun showAchievementDialog(notification: Notification) {
        val data = (notification.data as? AchievementData) ?: return

        val dialog = AchievementDialog(activity)
        dialog.isLastOnboardingAchievement = data.isLastOnboardingAchievement
        val canShow = dialog.setType(data.achievement ?: "", data.message, data.modalText)
        if (!canShow) return

        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            lifecycleScope.launch(context = Dispatchers.Main) {
                dialog.enqueue()
            }
        }
    }

    fun showOnboardingCompletedDialog(notification: Notification) {
        val dialog = AchievementDialog(activity)
        dialog.isLastOnboardingAchievement = true
        dialog.setType(notification.type ?: "", null, null)

        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            lifecycleScope.launch(context = Dispatchers.Main) {
                dialog.enqueue()
            }
        }
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

    private fun showRebirthEnabledDialog() {
        lifecycleScope.launch(context = Dispatchers.Main) {
            val dialog = RebirthEnabledDialog(activity)
            dialog.enqueue()
        }
    }

    private fun showRebirthAchievementDialog() {
        lifecycleScope.launch(context = Dispatchers.Main) {
            val user = userRepository.retrieveUser(forced = true)
            val dialog = RebirthAchievementDialog(activity, user)
            dialog.enqueue()
        }
    }
}
