package com.habitrpg.wearos.habitica.ui.viewHolders

import android.content.res.ColorStateList
import android.view.View
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.RowHubBinding
import com.habitrpg.wearos.habitica.models.user.MenuItem

class HubViewHolder(itemView: View): BindableViewHolder<MenuItem>(itemView) {
    val binding = RowHubBinding.bind(itemView)

    override fun bind(data: MenuItem) {
        binding.title.text = data.title
        binding.detailView.text = data.detailText
        binding.title.setTextColor(data.textColor)
        binding.detailView.setTextColor(data.textColor)
        binding.iconView.setImageDrawable(data.icon)
        if (data.isProminent) {
            binding.iconView.setColorFilter(ContextCompat.getColor(itemView.context, R.color.black))
            binding.rowContainer.backgroundTintList = ColorStateList.valueOf(data.color)
            binding.detailView.setTextColor(data.textColor)
        } else {
            binding.iconView.setColorFilter(data.color)
            binding.rowContainer.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.surface)
            binding.detailView.setTextColor(ContextCompat.getColor(itemView.context, R.color.watch_purple_200))
        }
        binding.root.setOnClickListener {
            data.onClick()
        }
    }
}