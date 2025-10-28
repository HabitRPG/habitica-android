package com.habitrpg.android.habitica.ui.views.dialogs

import android.content.Context
import android.view.View
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.DialogAchievementDetailBinding
import com.habitrpg.common.habitica.extensions.fromHtml
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.extensions.loadImage

class RebirthEnabledDialog(context: Context) : HabiticaAlertDialog(context) {
    private val binding: DialogAchievementDetailBinding =
        DialogAchievementDetailBinding.inflate(context.layoutInflater)

    init {
        binding.titleView.visibility = View.VISIBLE
        setAdditionalContentView(binding.root)
        configure()
    }

    private fun configure() {
        setTitle(R.string.rebirth_enabled_title)
        binding.titleView.text = context.getString(R.string.rebirth_enabled_title)
        binding.descriptionView.text = context.getString(R.string.rebirth_enabled_description).fromHtml()
        binding.iconView.loadImage("rebirth_orb")
        binding.achievementWrapper.visibility = View.VISIBLE
        binding.onboardingDoneIcon.visibility = View.GONE

        addButton(R.string.onwards, isPrimary = true, isDestructive = false) { _, _ ->
        }
    }
}
