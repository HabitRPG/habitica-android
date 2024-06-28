package com.habitrpg.wearos.habitica.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.databinding.RowHabitBinding
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.wearos.habitica.ui.viewHolders.tasks.HabitViewHolder

class HabitListAdapter : TaskListAdapter() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return if (viewType == 1) {
            return HabitViewHolder(RowHabitBinding.inflate(parent.context.layoutInflater, parent, false).root)
        } else {
            super.onCreateViewHolder(parent, viewType)
        }
    }
}
