package com.habitrpg.wearos.habitica.ui.viewHolders

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.databinding.RowTaskHeaderBinding

class HeaderTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(title: String, showDone: Boolean, isDisconnected: Boolean) {
        binding.header.textView.text = title
        binding.doneView.isVisible = showDone
        binding.disconnected.root.isVisible = isDisconnected
    }

    val binding = RowTaskHeaderBinding.bind(itemView)
}
