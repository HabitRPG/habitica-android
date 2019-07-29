package com.habitrpg.android.habitica.ui.views.social

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.extensions.setScaledPadding
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.inventory.QuestProgress
import com.habitrpg.android.habitica.models.inventory.QuestProgressCollect
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.HabiticaIcons
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.ValueBar
import kotlinx.android.synthetic.main.value_bar.view.*

class OldQuestProgressView : LinearLayout {

    private val bossNameView: TextView by bindView(R.id.bossNameView)
    private val bossHealthView: ValueBar by bindView(R.id.bossHealthView)
    private val bossRageView: ValueBar by bindView(R.id.bossRageView)
    private val collectionContainer: ViewGroup by bindView(R.id.collectionContainer)

    private val rect = RectF()
    private val displayDensity = context.resources.displayMetrics.density
    private val lightGray = ContextCompat.getColor(context, R.color.gray_700)
    private val mediumGray = ContextCompat.getColor(context, R.color.gray_600)
    private val darkGray = ContextCompat.getColor(context, R.color.gray_400)

    constructor(context: Context) : super(context) {
        setupView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupView(context)
    }

    private fun setupView(context: Context) {
        setWillNotDraw(false)
        inflate(R.layout.quest_progress_old, true)

        orientation = VERTICAL

        setScaledPadding(context, 16, 16, 16, 16)

        bossHealthView.setSecondaryIcon(HabiticaIconsHelper.imageOfHeartLightBg())
        bossHealthView.setDescriptionIcon(HabiticaIconsHelper.imageOfDamage())
        bossRageView.setSecondaryIcon(HabiticaIconsHelper.imageOfRage())
    }

    override fun onDraw(canvas: Canvas?) {
        rect.set(0.0f, 0.0f, (canvas?.width?.toFloat()
                ?: 1.0f) / displayDensity, (canvas?.height?.toFloat()
                ?: 1.0f) / displayDensity)
        canvas?.scale(displayDensity, displayDensity)
        HabiticaIcons.drawQuestBackground(canvas, rect, lightGray, darkGray, mediumGray)
        canvas?.scale(1.0f / displayDensity, 1.0f / displayDensity)
        super.onDraw(canvas)
    }

    fun setData(quest: QuestContent, progress: QuestProgress?) {
        collectionContainer.removeAllViews()
        if (quest.isBossQuest) {
            bossNameView.text = quest.boss?.name
            if (progress != null) {
                bossHealthView.set(progress.hp, quest.boss?.hp?.toDouble() ?: 0.0)
            }
            if (quest.boss?.hasRage() == true) {
                bossRageView.visibility = View.VISIBLE
                bossRageView.set(progress?.rage ?: 0.0, quest.boss?.rage?.value ?: 0.0)
            } else {
                bossRageView.visibility = View.GONE
            }
            bossNameView.visibility = View.VISIBLE
            bossHealthView.visibility = View.VISIBLE
        } else {
            bossNameView.visibility = View.GONE
            bossHealthView.visibility = View.GONE
            bossRageView.visibility = View.GONE

            if (progress != null) {
                val inflater = LayoutInflater.from(context)
                for (collect in progress.collect ?: emptyList<QuestProgressCollect>()) {
                    val contentCollect = quest.getCollectWithKey(collect.key) ?: continue
                    val view = inflater.inflate(R.layout.quest_collect, collectionContainer, false)
                    val iconView = view.findViewById(R.id.icon_view) as? SimpleDraweeView
                    val nameView = view.findViewById(R.id.name_view) as? TextView
                    val valueView = view.findViewById(R.id.value_view) as? ValueBar
                    DataBindingUtils.loadImage(iconView, "quest_" + quest.key + "_" + collect.key)
                    nameView?.text = contentCollect.text
                    valueView?.set(collect.count.toDouble(), contentCollect.count.toDouble())

                    collectionContainer.addView(view)
                }
            }
        }
    }

    fun configure(user: User, userOnQuest: Boolean?) {
        val value = (user.party?.quest?.progress?.up ?: 0F).toDouble()
        if (userOnQuest == true) {
            bossHealthView.pendingValue = value
            bossHealthView.description = String.format("%.01f dmg pending", value)
            bossHealthView.descriptionIconView.visibility = View.VISIBLE
        } else {
            bossHealthView.pendingValue = 0.0
            bossHealthView.description = ""
            bossHealthView.descriptionIconView.visibility = View.GONE
        }
    }

}
