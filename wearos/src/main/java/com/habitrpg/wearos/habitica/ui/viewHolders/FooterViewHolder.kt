package com.habitrpg.wearos.habitica.ui.viewHolders

import android.view.View
import com.habitrpg.android.habitica.databinding.RowFooterBinding

class FooterViewHolder(itemView: View) : BindableViewHolder<String>(itemView) {
    private val binding = RowFooterBinding.bind(itemView)

    override fun bind(data: String) {
        binding.textView.text = data
    }
}
