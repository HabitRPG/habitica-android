package com.habitrpg.android.habitica.ui.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.AchievementQuestItemBinding
import com.habitrpg.android.habitica.databinding.AchievementSectionHeaderBinding
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.shared.habitica.models.QuestAchievement
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.views.dialogs.AchievementDetailDialog

class AchievementsAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var useGridLayout: Boolean = false
    var entries = listOf<Any>()
    var questAchievements = listOf<QuestAchievement>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> SectionViewHolder(parent.inflate(R.layout.achievement_section_header))
            3 -> QuestAchievementViewHolder(parent.inflate(R.layout.achievement_quest_item))
            else -> AchievementViewHolder(if (useGridLayout) {
                parent.inflate(R.layout.achievement_grid_item)
            } else {
                parent.inflate(R.layout.achievement_list_item)
            })
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when {
            entries.size > position -> when (val entry = entries[position]) {
                is Achievement -> (holder as? AchievementViewHolder)?.bind(entry)
                is Pair<*, *> -> (holder as? SectionViewHolder)?.bind(entry)
            }
            entries.size == position -> (holder as? SectionViewHolder)?.bind(Pair("Quests completed", questAchievements.size))
            else -> (holder as? QuestAchievementViewHolder)?.bind(questAchievements[position - 1 - entries.size])
        }
    }

    override fun getItemCount(): Int {
        return entries.size + questAchievements.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            entries.size > position -> {
                val entry = entries[position]
                if (entry is Pair<*, *>) {
                    0
                } else {
                    if (useGridLayout) 1 else 2
                }
            }
            entries.size == position -> 0
            else -> 3
        }
    }

    class SectionViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private var binding = AchievementSectionHeaderBinding.bind(itemView)

        fun bind(category: Pair<*, *>) {
            binding.title.text = category.first as? String
            binding.countLabel.text = category.second.toString()
        }
    }

    class AchievementViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var achievement: Achievement? = null

        private val achievementContainer: ViewGroup?
        private val achievementIconView: SimpleDraweeView
        private val achievementCountView: TextView
        private val achievementTitleView: TextView
        private val achievementDescriptionView: TextView?

        init {
            itemView.setOnClickListener(this)
            achievementContainer = itemView.findViewById(R.id.achievement_container)
            achievementIconView = itemView.findViewById(R.id.achievement_icon)
            achievementCountView = itemView.findViewById(R.id.achievement_count_label)
            achievementTitleView = itemView.findViewById(R.id.achievement_title)
            achievementDescriptionView = itemView.findViewById(R.id.achievement_description)
        }

        fun bind(achievement: Achievement) {
            this.achievement = achievement
            val iconName = if (achievement.earned) {
                achievement.icon + "2x"
            } else {
                "achievement-unearned2x"
            }
            DataBindingUtils.loadImage(achievementIconView, iconName)
            achievementTitleView.text = achievement.title
            achievementDescriptionView?.text = achievement.text
            if (achievement.optionalCount ?: 0 > 0) {
                achievementCountView.visibility = View.VISIBLE
                achievementCountView.text = achievement.optionalCount.toString()
            } else {
                achievementCountView.visibility = View.GONE
            }
            achievementContainer?.clipToOutline = true
        }

        override fun onClick(v: View?) {
            achievement?.let {
                AchievementDetailDialog(it, itemView.context).show()
            }
        }
    }

    class QuestAchievementViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private var binding = AchievementQuestItemBinding.bind(itemView)
        private var achievement: QuestAchievement? = null

        fun bind(achievement: QuestAchievement) {
            this.achievement = achievement
            binding.achievementTitle.text = achievement.title
            binding.achievementCountLabel.text = achievement.count.toString()
        }
    }
}
