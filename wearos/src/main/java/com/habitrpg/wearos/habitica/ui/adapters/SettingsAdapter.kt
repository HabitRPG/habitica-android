package com.habitrpg.wearos.habitica.ui.adapters

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.RowFooterBinding
import com.habitrpg.android.habitica.databinding.RowSettingsBinding
import com.habitrpg.android.habitica.databinding.RowSpacerBinding
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.wearos.habitica.ui.viewHolders.BindableViewHolder
import com.habitrpg.wearos.habitica.ui.viewHolders.FooterViewHolder
import com.habitrpg.wearos.habitica.ui.viewHolders.SpacerViewHolder

class SettingsAdapter : BaseAdapter<SettingsItem>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> FooterViewHolder(RowFooterBinding.inflate(parent.context.layoutInflater, parent, false).root)
            2 -> SpacerViewHolder(RowSpacerBinding.inflate(parent.context.layoutInflater, parent, false).root)
            3 -> SettingsViewHolder(RowSettingsBinding.inflate(parent.context.layoutInflater, parent, false).root)
            else -> super.onCreateViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (holder) {
            is SettingsViewHolder -> {
                holder.bind(getItemAt(position))
            }
            is FooterViewHolder -> {
                holder.bind(getItemAt(position).title)
            }
            is SpacerViewHolder -> {
                holder.bind(16.dpToPx(holder.itemView.context))
            }
            else -> super.onBindViewHolder(holder, position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) return TYPE_HEADER
        val item = getItemAt(position)
        return when (item.type) {
            SettingsItem.Types.HEADER -> TYPE_HEADER
            SettingsItem.Types.FOOTER -> 1
            SettingsItem.Types.SPACER -> 2
            else -> 3
        }
    }
}

class SettingsViewHolder(itemView: View) : BindableViewHolder<SettingsItem>(itemView) {
    private val binding = RowSettingsBinding.bind(itemView)

    private var widget: View? = null

    override fun bind(data: SettingsItem) {
        if (widget != null) {
            (widget?.parent as? ViewGroup)?.removeView(widget)
            widget = null
        }
        binding.titleView.text = data.title
        if (data.type == SettingsItem.Types.DESTRUCTIVE_BUTTON) {
            binding.titleView.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_red))
        } else {
            binding.titleView.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
        }

        if (data.type == SettingsItem.Types.TOGGLE) {
            val switch = SwitchCompat(itemView.context)
            switch.isChecked = data.value as? Boolean == true
            switch.isClickable = false
            switch.showText = false
            widget = switch
            binding.row.addView(switch)

            if (data.value as? Boolean == true) {
                binding.row.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(itemView.context, R.color.watch_purple_100))
                binding.row.background.alpha = 102
            } else {
                binding.row.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(itemView.context, R.color.watch_purple_5))
                binding.row.background.alpha = 255
            }
        }

        binding.root.setOnClickListener {
            data.onTap()
        }
    }
}

data class SettingsItem(
    val identifier: String,
    val title: String,
    val type: Types,
    var value: Any?,
    val onTap: () -> Unit
) {
    enum class Types {
        BUTTON,
        DESTRUCTIVE_BUTTON,
        SPACER,
        TOGGLE,
        HEADER,
        FOOTER
    }
}
