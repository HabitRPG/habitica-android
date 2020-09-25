package com.habitrpg.android.habitica.ui.views.social

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.habitrpg.android.habitica.R
<<<<<<< HEAD
=======
import com.habitrpg.android.habitica.databinding.QuestMenuViewBinding
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.models.inventory.Quest
>>>>>>> develop
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.shared.habitica.models.inventory.Quest
import com.habitrpg.shared.habitica.models.user.User
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper

class QuestMenuView : LinearLayout {
    private val binding = QuestMenuViewBinding.inflate(context.layoutInflater, this, true)

    private var questContent: QuestContent? = null

    constructor(context: Context) : super(context) {
        setupView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupView(context)
    }

    private fun setupView(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.quest_menu_view, this)

        binding.heartIconView.setImageBitmap(HabiticaIconsHelper.imageOfHeartDarkBg())

        binding.pendingDamageIconView.setImageBitmap(HabiticaIconsHelper.imageOfDamage())

        binding.closeButton.setOnClickListener {
            hideBossArt()
            val preferences = context.getSharedPreferences("collapsible_sections", 0)
            preferences?.edit {
                putBoolean("boss_art_collapsed", true)
            }
        }
    }

    fun configure(quest: Quest) {
        binding.healthBarView.setCurrentValue(quest.progress?.hp ?: 0.0)
    }

    fun configure(questContent: QuestContent) {
        this.questContent = questContent
        binding.healthBarView.setMaxValue(questContent.boss?.hp?.toDouble() ?: 0.0)
        binding.bottomView.setBackgroundColor(questContent.colors?.darkColor ?: 0)
        binding.bossArtView.setBackgroundColor(questContent.colors?.mediumColor ?: 0)
        DataBindingUtils.loadImage(binding.bossArtView, "quest_"+questContent.key)
        binding.bossNameView.text = questContent.boss?.name
    }

    fun configure(user: User) {
        binding.pendingDamageTextView.text = String.format("%.01f", (user.party?.quest?.progress?.up ?: 0f))
    }

    fun hideBossArt() {
        binding.topView.orientation = HORIZONTAL
        binding.topView.setBackgroundColor(questContent?.colors?.mediumColor ?: 0)
        binding.bossNameView.gravity = Gravity.START
        binding.bossNameView.layoutParams = LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1F)
        binding.bossArtView.visibility = View.GONE
        binding.typeTextView.setTextColor(questContent?.colors?.extraLightColor ?: 0)
        binding.closeButton.visibility = View.GONE
    }

    fun showBossArt() {
        binding.topView.orientation = VERTICAL
        binding.topView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
        binding.bossNameView.gravity = Gravity.END
        binding.bossNameView.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        binding.bossArtView.visibility = View.VISIBLE
        binding.typeTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
        binding.closeButton.visibility = View.VISIBLE
    }
}