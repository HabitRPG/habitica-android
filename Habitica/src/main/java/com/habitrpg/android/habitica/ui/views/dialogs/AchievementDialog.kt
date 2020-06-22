package com.habitrpg.android.habitica.ui.views.dialogs

import android.content.Context
import android.os.Build
import android.text.Html
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.DialogAchievementDetailBinding
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.Notification
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils

class AchievementDialog(context: Context) : HabiticaAlertDialog(context) {

    private val binding: DialogAchievementDetailBinding = DialogAchievementDetailBinding.inflate(context.layoutInflater)

    init {
        binding.titleView.visibility = View.VISIBLE
        setAdditionalContentView(binding.root)
    }

    fun setType(type: String) {
        when (type) {
            Notification.Type.ACHIEVEMENT_PARTY_UP.type -> configure(R.string.partyUpTitle, R.string.partyUpDescription, "partyUp")
            Notification.Type.ACHIEVEMENT_PARTY_ON.type -> configure(R.string.partyOnTitle, R.string.partyOnDescription, "partyOn")
            Notification.Type.ACHIEVEMENT_BEAST_MASTER.type -> configure(R.string.beastMasterTitle, R.string.beastMasterDescription, "rat")
            Notification.Type.ACHIEVEMENT_MOUNT_MASTER.type -> configure(R.string.mountMasterTitle, R.string.mountMasterDescription, "wolf")
            Notification.Type.ACHIEVEMENT_TRIAD_BINGO.type -> configure(R.string.triadBingoTitle, R.string.triadBingoDescription, "triadbingo")
            Notification.Type.ACHIEVEMENT_GUILD_JOINED.type -> configure(R.string.joinedGuildTitle, R.string.joinedGuildDescription, "guild")
            Notification.Type.ACHIEVEMENT_CHALLENGE_JOINED.type -> configure(R.string.joinedChallengeTitle, R.string.joinedChallengeDescription, "challenge")
            Notification.Type.ACHIEVEMENT_INVITED_FRIEND.type -> configure(R.string.inviteFriendTitle, R.string.inviteFriendDescription, "friends")
            "createdTask" -> configure(R.string.createdTaskTitle, R.string.createdTaskDescription, type)
            "completedTask" -> configure(R.string.completedTaskTitle, R.string.completedTaskDescription, type)
            "hatchedPet" -> configure(R.string.hatchedPetTitle, R.string.hatchedPetDescription, type)
            "fedPet" -> configure(R.string.fedPetTitle, R.string.fedPetDescription, type)
            "purchasedEquipment" -> configure(R.string.purchasedEquipmentTitle, R.string.purchasedEquipmentDescription, type)
            Notification.Type.ACHIEVEMENT_ONBOARDING_COMPLETE.type -> configure(R.string.onboardingCompleteTitle, R.string.onboardingCompleteDescription, "onboardingComplete")
        }
    }

    private fun configure(titleID: Int, descriptionID: Int, iconName: String) {
        binding.titleView.text = context.getString(titleID)
        binding.descriptionView.text = context.getString(descriptionID)
        DataBindingUtils.loadImage(binding.iconView, "achievement-${iconName}2x")
        if (iconName == "onboardingComplete") {
            setTitle(R.string.onboardingComplete_achievement_title)
            binding.titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP,14f)
            binding.achievementWrapper.visibility = View.GONE
            binding.onboardingDoneIcon.visibility = View.VISIBLE
            val titleText = context.getString(titleID)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.titleView.setText(Html.fromHtml(titleText,  Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
            } else {
                binding.titleView.setText(Html.fromHtml(titleText), TextView.BufferType.SPANNABLE)
            }
        } else {
            setTitle(R.string.achievement_title)
            binding.achievementWrapper.visibility = View.VISIBLE
            binding.onboardingDoneIcon.visibility = View.GONE
        }

        if (User.ONBOARDING_ACHIEVEMENT_KEYS.contains(iconName)) {

            addButton(R.string.onwards, isPrimary = true, isDestructive = false) { _, _ ->
            }
            addButton(R.string.view_onboarding_tasks, false, false) { _, _ ->
                MainNavigationController.navigate(R.id.adventureGuideActivity)
            }
        } else {
            addButton(R.string.view_achievements, isPrimary = true, isDestructive = false) { _, _ ->
                MainNavigationController.navigate(R.id.achievementsFragment)
            }
            addButton(R.string.close, false)
        }
    }
}
