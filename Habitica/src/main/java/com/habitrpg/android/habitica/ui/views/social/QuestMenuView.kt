package com.habitrpg.android.habitica.ui.views.social

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.HabiticaProgressBar

class QuestMenuView : LinearLayout {

    private val bossArtView: SimpleDraweeView by bindView(R.id.bossArtView)
    private val bossNameView: TextView by bindView(R.id.bossNameView)
    private val typeTextView: TextView by bindView(R.id.typeTextView)
    private val heartIconView: ImageView by bindView(R.id.heartIconView)
    private val healthBarView: HabiticaProgressBar by bindView(R.id.healthBarView)
    private val topView: LinearLayout by bindView(R.id.topView)
    private val closeButton: AppCompatImageButton by bindView(R.id.closeButton)
    private val bottomView: ViewGroup by bindView(R.id.bottomView)
    private val pendingDamageIconView: ImageView by bindView(R.id.pendingDamageIconView)
    private val pendingDamageTextView: TextView by bindView(R.id.pendingDamageTextView)

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

        heartIconView.setImageBitmap(HabiticaIconsHelper.imageOfHeartDarkBg())

        pendingDamageIconView.setImageBitmap(HabiticaIconsHelper.imageOfDamage())

        closeButton.setOnClickListener {
            hideBossArt()
            val preferences = context.getSharedPreferences("collapsible_sections", 0)
            preferences?.edit {
                putBoolean("boss_art_collapsed", true)
            }
        }
    }

    fun configure(quest: Quest) {
        healthBarView.currentValue = quest.progress?.hp ?: 0.0
    }

    fun configure(questContent: QuestContent) {
        this.questContent = questContent
        healthBarView.maxValue = questContent.boss?.hp?.toDouble() ?: 0.0
        bottomView.setBackgroundColor(questContent.colors?.darkColor ?: 0)
        bossArtView.setBackgroundColor(questContent.colors?.mediumColor ?: 0)
        DataBindingUtils.loadImage(bossArtView, "quest_"+questContent.key)
        bossNameView.text = questContent.boss?.name
    }

    fun configure(user: User) {
        pendingDamageTextView.text = String.format("%.01f", (user.party?.quest?.progress?.up ?: 0f))
    }

    fun hideBossArt() {
        topView.orientation = HORIZONTAL
        topView.setBackgroundColor(questContent?.colors?.mediumColor ?: 0)
        bossNameView.gravity = Gravity.LEFT
        bossNameView.layoutParams = LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1F)
        bossArtView.visibility = View.GONE
        typeTextView.setTextColor(questContent?.colors?.extraLightColor ?: 0)
        closeButton.visibility = View.GONE
    }

    fun showBossArt() {
        topView.orientation = VERTICAL
        topView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
        bossNameView.gravity = Gravity.RIGHT
        bossNameView.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        bossArtView.visibility = View.VISIBLE
        typeTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
        closeButton.visibility = View.VISIBLE
    }
}