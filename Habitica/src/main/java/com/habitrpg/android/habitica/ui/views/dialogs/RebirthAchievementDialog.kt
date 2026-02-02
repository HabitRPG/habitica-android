package com.habitrpg.android.habitica.ui.views.dialogs

import android.content.Context
import android.view.View
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.DialogAchievementDetailBinding
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.common.habitica.extensions.fromHtml
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.helpers.MainNavigationController

class RebirthAchievementDialog(context: Context, private val user: User?) : HabiticaAlertDialog(context) {
    private val binding: DialogAchievementDetailBinding =
        DialogAchievementDetailBinding.inflate(context.layoutInflater)

    init {
        binding.titleView.visibility = View.VISIBLE
        setAdditionalContentView(binding.root)
        configure()
    }

    private fun configure() {
        setTitle(R.string.achievement_title)
        binding.titleView.text = context.getString(R.string.rebirth_achievement_title)

        val rebirthCount = user?.rebirths ?: 0
        val rebirthLevel = user?.rebirthLevel ?: 0
        val description = if (rebirthLevel >= 100) {
            context.getString(R.string.rebirth_achievement_description_max, rebirthCount, 100)
        } else {
            context.getString(R.string.rebirth_achievement_description, rebirthCount, rebirthLevel)
        }

        binding.descriptionView.text = description.fromHtml()
        binding.iconView.loadImage("achievement-sun2x")
        binding.achievementWrapper.visibility = View.VISIBLE
        binding.onboardingDoneIcon.visibility = View.GONE

        addButton(R.string.view_achievements, isPrimary = true, isDestructive = false) { _, _ ->
            MainNavigationController.navigate(R.id.achievementsFragment)
        }
        addButton(R.string.close, false)
    }
}
