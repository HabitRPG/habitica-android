package com.habitrpg.android.habitica.ui.views.social

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.QuestCollectBinding
import com.habitrpg.android.habitica.databinding.QuestProgressOldBinding
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.inventory.QuestProgress
import com.habitrpg.android.habitica.models.inventory.QuestProgressCollect
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.extensions.setScaledPadding

class OldQuestProgressView : LinearLayout {
    private val binding = QuestProgressOldBinding.inflate(context.layoutInflater, this)

    constructor(context: Context) : super(context) {
        setupView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupView(context)
    }

    private fun setupView(context: Context) {
        orientation = VERTICAL
        binding.bossHealthView.valueSuffix = "HP"
        binding.bossRageView.valueSuffix = context.getString(R.string.rage)
        binding.bossHealthView
        setScaledPadding(context, 16, 16, 16, 16)
    }

    fun setData(
        quest: QuestContent,
        progress: QuestProgress?
    ) {
        binding.collectionContainer.removeAllViews()
        if (quest.isBossQuest) {
            binding.bossNameView.text = quest.boss?.name
            binding.bossHealthView.barHeight = 5.dpToPx(context)
            if (progress != null) {
                binding.bossHealthView.set(progress.hp, quest.boss?.hp?.toDouble() ?: 0.0)
            }
            if (quest.boss?.hasRage == true) {
                binding.bossRageWrapper.visibility = VISIBLE
                binding.bossRageView.barHeight = 5.dpToPx(context)
                binding.bossRageView
                binding.bossRageView.set(progress?.rage ?: 0.0, quest.boss?.rage?.value ?: 0.0)
                binding.bossRageNameView.text = quest.boss?.rage?.title
            } else {
                binding.bossRageWrapper.visibility = GONE
            }
            binding.bossHealthWrapper.visibility = VISIBLE
        } else {
            binding.bossHealthWrapper.visibility = GONE
            binding.bossRageWrapper.visibility = GONE

            if (progress != null) {
                val inflater = LayoutInflater.from(context)
                for (collect in progress.collect ?: emptyList<QuestProgressCollect>()) {
                    val contentCollect = quest.getCollectWithKey(collect.key) ?: continue
                    val collectBinding =
                        QuestCollectBinding.inflate(inflater, binding.collectionContainer, true)
                    collectBinding.iconView.loadImage("quest_" + quest.key + "_" + collect.key)
                    collectBinding.nameView.text = contentCollect.text
                    collectBinding.valueView.set(
                        collect.count.toDouble(),
                        contentCollect.count.toDouble()
                    )
                    collectBinding.valueView.barHeight = 5.dpToPx(context)
                }
            }
        }
    }
}
