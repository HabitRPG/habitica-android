package com.habitrpg.wearos.habitica.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.databinding.RowRewardBinding
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.wearos.habitica.ui.viewHolders.tasks.RewardViewHolder

class RewardListAdapter : TaskListAdapter() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        return if (viewType == 1) {
            return RewardViewHolder(RowRewardBinding.inflate(parent.context.layoutInflater, parent, false).root)
        } else {
            super.onCreateViewHolder(parent, viewType)
        }
    }
}
