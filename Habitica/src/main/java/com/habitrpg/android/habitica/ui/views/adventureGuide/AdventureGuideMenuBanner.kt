package com.habitrpg.android.habitica.ui.views.adventureGuide

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.AdventureGuideMenuBannerBinding
import com.habitrpg.android.habitica.extensions.fromHtml
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.models.user.User

class AdventureGuideMenuBanner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var binding = AdventureGuideMenuBannerBinding.inflate(context.layoutInflater, this)

    init {
        background = ContextCompat.getDrawable(context, R.drawable.adventure_guide_menu_bg)
        val descriptionText = context.getString(R.string.complete_for_gold)
        binding.descriptionView.setText(descriptionText.fromHtml(), TextView.BufferType.SPANNABLE)
    }

    fun updateData(user: User) {
        val achievements = user.onboardingAchievements
        val completed = achievements.count { it.earned }
        binding.progressBar.max = achievements.size
        binding.progressBar.progress = completed
        binding.countView.text = "$completed / ${achievements.size}"
    }
}
