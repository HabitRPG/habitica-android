package com.habitrpg.wearos.habitica.ui.adapters

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.RowHeaderBinding
import com.habitrpg.android.habitica.databinding.RowSettingsBinding
import com.habitrpg.android.habitica.databinding.RowSpacerBinding
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.wearos.habitica.ui.viewHolders.BindableViewHolder
import com.habitrpg.wearos.habitica.ui.viewHolders.HeaderViewHolder
import com.habitrpg.wearos.habitica.ui.viewHolders.SpacerViewHolder

class SettingsAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var data = listOf<SettingsItem>()
    set(value) {
        field = value
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0) {
            return HeaderViewHolder(RowHeaderBinding.inflate(parent.context.layoutInflater, parent, false).root)
        } else if (viewType == 1) {
            return SpacerViewHolder(RowSpacerBinding.inflate(parent.context.layoutInflater, parent, false).root)
        } else {
            return SettingsViewHolder(RowSettingsBinding.inflate(parent.context.layoutInflater, parent, false).root)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SettingsViewHolder) {
            holder.bind(data[position])
        } else if (holder is HeaderViewHolder) {
            holder.bind(data[position].title)
        } else if (holder is SpacerViewHolder) {
            holder.bind(16.dpToPx(holder.itemView.context))
        }
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int {
        val item = data[position]
        return when (item.type) {
            SettingsItem.Types.HEADER -> 0
            SettingsItem.Types.SPACER -> 1
            else -> 2
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
            val radio = RadioButton(itemView.context)
            radio.isChecked = data.value as? Boolean == true
            radio.isEnabled = false
            widget = radio
            binding.row.addView(radio)

            if (data.value as? Boolean == true) {
                binding.row.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(itemView.context, R.color.watch_purple_100))
                binding.row.background.alpha = 127
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
        HEADER
    }
}
