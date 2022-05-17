package com.habitrpg.android.habitica.ui.views.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.habitrpg.android.habitica.databinding.DialogAchievementDetailBinding
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.extensions.fromHtml
import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.views.PixelArtView

class AchievementDetailDialog(val achievement: Achievement, context: Context) : HabiticaAlertDialog(context) {

    private var iconView: PixelArtView?
    private var descriptionView: TextView?

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = DialogAchievementDetailBinding.inflate(inflater)
        binding.onboardingDoneIcon.visibility = View.GONE
        iconView = binding.iconView
        descriptionView = binding.descriptionView
        setAdditionalContentView(binding.root)
        setTitle(achievement.title)
        descriptionView?.setText(achievement.text?.fromHtml(), TextView.BufferType.SPANNABLE)
        val iconName = if (achievement.earned) {
            achievement.icon + "2x"
        } else {
            "achievement-unearned2x"
        }
        iconView?.loadImage(iconName)
        addCloseButton(true)
    }
}
