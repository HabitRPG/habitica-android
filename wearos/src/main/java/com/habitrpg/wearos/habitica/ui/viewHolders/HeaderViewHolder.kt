package com.habitrpg.wearos.habitica.ui.viewHolders

import android.view.View
import com.habitrpg.android.habitica.databinding.RowHeaderBinding

class HeaderViewHolder(itemView: View): BindableViewHolder<String>(itemView) {
    private val binding = RowHeaderBinding.bind(itemView)

    override fun bind(data: String) {
        binding.textView.text = data
    }
}