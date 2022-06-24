package com.habitrpg.wearos.habitica.ui.viewHolders

import android.view.View
import com.habitrpg.android.habitica.databinding.RowSpacerBinding

class SpacerViewHolder(itemView: View): BindableViewHolder<Int>(itemView) {
    private val binding = RowSpacerBinding.bind(itemView)

    override fun bind(data: Int) {
        binding.root.layoutParams.height = data
    }
}