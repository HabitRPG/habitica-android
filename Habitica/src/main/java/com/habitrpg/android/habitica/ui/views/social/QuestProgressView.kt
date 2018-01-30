package com.habitrpg.android.habitica.ui.views.social

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.bindView
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.inventory.QuestProgress
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.views.ValueBar

class QuestProgressView : LinearLayout {

    private val bossNameView: TextView by bindView(R.id.bossNameView)
    private val bossHealthView: ValueBar by bindView(R.id.bossHealthView)
    private val rageMeterView: TextView by bindView(R.id.rageMeterView)
    private val bossRageView: ValueBar by bindView(R.id.bossRageView)
    private val collectionContainer: ViewGroup by bindView(R.id.collectionContainer)

    var quest: QuestContent? = null
    set(value) {
        field = value
        configure()
    }
    var progress: QuestProgress? = null
    set(value) {
        field = value
        configure()
    }

    constructor(context: Context) : super(context) {
        setupView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupView(context)
    }

    private fun setupView(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.quest_progress, this)
    }

    fun setData(quest: QuestContent, progress: QuestProgress?) {
    }

    private fun configure() {
        val quest = this.quest
        val progress = this.progress
        if (quest == null || progress == null) {
            return
        }
        collectionContainer.removeAllViews()
        if (quest.isBossQuest) {
            bossNameView.text = quest.boss.name
            bossNameView.visibility = View.VISIBLE
            bossHealthView.visibility = View.VISIBLE
            bossHealthView.set(progress.hp, quest.boss?.hp?.toDouble() ?: 0.0)

            if (quest.boss.hasRage()) {
                rageMeterView.visibility = View.VISIBLE
                bossRageView.visibility = View.VISIBLE
                rageMeterView.text = quest.boss.rage?.title
                bossRageView.set(progress.rage, quest.boss?.rage?.value ?: 0.0)
            } else {
                rageMeterView.visibility = View.GONE
                bossRageView.visibility = View.GONE
            }
        } else {
            bossNameView.visibility = View.GONE
            bossHealthView.visibility = View.GONE
            rageMeterView.visibility = View.GONE
            bossRageView.visibility = View.GONE

            val inflater = LayoutInflater.from(context)
            for (collect in progress.collect) {
                val contentCollect = quest.getCollectWithKey(collect.key) ?: continue
                val view = inflater.inflate(R.layout.quest_collect, collectionContainer, false)
                val iconView = view.findViewById<View>(R.id.icon_view) as SimpleDraweeView
                val nameView = view.findViewById<View>(R.id.name_view) as TextView
                val valueView = view.findViewById<View>(R.id.value_view) as ValueBar
                DataBindingUtils.loadImage(iconView, "quest_" + quest.key + "_" + collect.key)
                nameView.text = contentCollect.text
                valueView.set(collect.count.toDouble(), contentCollect.count.toDouble())

                collectionContainer.addView(view)
            }
        }
    }

}
