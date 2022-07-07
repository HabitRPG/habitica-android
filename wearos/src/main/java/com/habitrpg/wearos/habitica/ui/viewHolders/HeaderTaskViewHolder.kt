package com.habitrpg.wearos.habitica.ui.viewHolders

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.databinding.RowTaskHeaderBinding

class HeaderTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(title: String, showDone: Boolean) {
        binding.header.textView.text = title
        binding.doneView.isVisible = showDone
    }

    val binding = RowTaskHeaderBinding.bind(itemView)
}
