package com.habitrpg.android.habitica.ui.views.social

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.bindView
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.ValueBar

class QuestMenuView : LinearLayout {

    private val bossNameView: TextView by bindView(R.id.bossNameView)
    private val typeTextView: TextView by bindView(R.id.typeTextView)
    private val healthBarView: ValueBar by bindView(R.id.healthBarView)
    private val topView: LinearLayout by bindView(R.id.topView)

    private var questContent: QuestContent? = null

    var collapsed = false
    set(value) {
        field = value
        if (field) {
            showBossArt()
        } else {
            hideBossArt()
        }
    }

    constructor(context: Context) : super(context) {
        setupView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupView(context)
    }

    private fun setupView(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.quest_menu_view, this)

        healthBarView.setIcon(HabiticaIconsHelper.imageOfHeartDarkBg())
        healthBarView.setLabelVisibility(View.GONE)
    }

    fun configure(quest: Quest) {
        healthBarView.currentValue = quest.progress?.hp ?: 0.0
    }

    fun configure(questContent: QuestContent) {
        this.questContent = questContent
        healthBarView.maxValue = questContent.boss.hp.toDouble()
        healthBarView.setBackgroundColor(questContent.colors?.darkColor ?: 0)
        topView.setBackgroundColor(questContent.colors?.mediumColor ?: 0)
        bossNameView.text = questContent.boss.name
    }

    fun hideBossArt() {
        topView.orientation = LinearLayout.HORIZONTAL
        bossNameView.gravity = Gravity.LEFT
        bossNameView.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1F)
        typeTextView.setTextColor(questContent?.colors?.extraLightColor ?: 0)
    }

    fun showBossArt() {
        topView.orientation = LinearLayout.VERTICAL
        bossNameView.gravity = Gravity.RIGHT
        bossNameView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        typeTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
    }
}