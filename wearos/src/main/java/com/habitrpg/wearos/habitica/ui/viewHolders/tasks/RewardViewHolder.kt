package com.habitrpg.wearos.habitica.ui.viewHolders.tasks

import android.view.View
import android.widget.TextView
import com.habitrpg.wearos.habitica.databinding.RowRewardBinding
import com.habitrpg.wearos.habitica.models.tasks.Task

class RewardViewHolder(itemView: View) : TaskViewHolder(itemView) {
    private val binding = RowRewardBinding.bind(itemView)
    override val titleView: TextView
        get() = binding.title

    override fun bind(data: Task) {
        super.bind(data)
    }
}