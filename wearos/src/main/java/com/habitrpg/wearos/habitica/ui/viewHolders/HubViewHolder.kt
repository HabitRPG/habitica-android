package com.habitrpg.wearos.habitica.ui.viewHolders

import android.content.res.ColorStateList
import android.view.View
import androidx.core.content.ContextCompat
import com.habitrpg.wearos.habitica.R
import com.habitrpg.wearos.habitica.databinding.RowHubBinding
import com.habitrpg.wearos.habitica.models.MenuItem

class HubViewHolder(itemView: View): BindableViewHolder<MenuItem>(itemView) {
    val binding = RowHubBinding.bind(itemView)

    override fun bind(data: MenuItem) {
        binding.title.text = data.title
        binding.iconView.setImageDrawable(data.icon)
        if (data.isProminent) {
            binding.iconView.setColorFilter(ContextCompat.getColor(itemView.context, R.color.white))
            binding.rowContainer.backgroundTintList = ColorStateList.valueOf(data.color)
        } else {
            binding.iconView.setColorFilter(data.color)
            binding.rowContainer.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.gray_5)
        }
        binding.root.setOnClickListener {
            data.onClick()
        }
    }
}