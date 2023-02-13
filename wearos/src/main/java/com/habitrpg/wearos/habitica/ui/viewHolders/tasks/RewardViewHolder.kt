package com.habitrpg.wearos.habitica.ui.viewHolders.tasks

import android.view.View
import com.habitrpg.android.habitica.databinding.RowRewardBinding
import com.habitrpg.wearos.habitica.ui.views.TaskTextView

class RewardViewHolder(itemView: View) : TaskViewHolder(itemView) {
    private val binding = RowRewardBinding.bind(itemView)
    override val titleView: TaskTextView
        get() = binding.title
}
