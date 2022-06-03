package com.habitrpg.android.habitica.ui.views.social

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.QuestMenuViewBinding
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.common.habitica.views.HabiticaIconsHelper
import java.util.Locale

class QuestMenuView : LinearLayout {
    private val binding = QuestMenuViewBinding.inflate(context.layoutInflater, this)

    private var questContent: QuestContent? = null

    constructor(context: Context) : super(context) {
        setupView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupView()
    }

    private fun setupView() {
        orientation = VERTICAL

        binding.heartIconView.setImageBitmap(HabiticaIconsHelper.imageOfHeartDarkBg())
        binding.rageIconView.setImageBitmap(HabiticaIconsHelper.imageOfRage())

        binding.pendingDamageIconView.setImageBitmap(HabiticaIconsHelper.imageOfDamage())
    }

    fun configure(quest: Quest) {
        binding.healthBarView.currentValue = quest.progress?.hp ?: 0.0
        binding.rageBarView.currentValue = quest.progress?.rage ?: 0.0
    }

    fun configure(questContent: QuestContent) {
        this.questContent = questContent
        binding.healthBarView.maxValue = questContent.boss?.hp?.toDouble() ?: 0.0
        binding.bossNameView.text = questContent.boss?.name
        binding.typeTextView.text = context.getString(R.string.boss_quest)

        if (questContent.boss?.hasRage == true) {
            binding.rageView.visibility = View.VISIBLE
            binding.rageBarView.maxValue = questContent.boss?.rage?.value ?: 0.0
        } else {
            binding.rageView.visibility = View.GONE
        }
    }

    fun configure(user: User) {
        binding.pendingDamageTextView.text = String.format(Locale.getDefault(), "%.01f", (user.party?.quest?.progress?.up ?: 0f))
    }

    fun hideBossArt() {
        binding.topView.orientation = HORIZONTAL
        binding.topView.setBackgroundColor(questContent?.colors?.mediumColor ?: 0)
        binding.bossNameView.gravity = Gravity.START
        binding.bossNameView.layoutParams = LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1F)
        binding.typeTextView.setTextColor(questContent?.colors?.extraLightColor ?: 0)
    }

    fun showBossArt() {
        binding.topView.orientation = VERTICAL
        binding.topView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
        binding.bossNameView.gravity = Gravity.END
        binding.bossNameView.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        binding.typeTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
    }
}
