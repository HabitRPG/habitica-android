package com.habitrpg.wearos.habitica.ui.adapters

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.RowSettingsBinding
import com.habitrpg.wearos.habitica.ui.viewHolders.BindableViewHolder

class SettingsAdapter: RecyclerView.Adapter<SettingsViewHolder>() {
    var data = listOf<SettingsItem>()
    set(value) {
        field = value
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        return SettingsViewHolder(RowSettingsBinding.inflate(parent.context.layoutInflater, parent, false).root)
    }

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
}

class SettingsViewHolder(itemView: View) : BindableViewHolder<SettingsItem>(itemView) {
    private val binding = RowSettingsBinding.bind(itemView)

    override fun bind(data: SettingsItem) {
        binding.titleView.text = data.title
        if (data.type == SettingsItem.Types.DESTRUCTIVE_BUTTON) {
            binding.titleView.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_red))
        } else {
            binding.titleView.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
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
    val value: Any?,
    val onTap: () -> Unit
) {
    enum class Types {
        BUTTON,
        DESTRUCTIVE_BUTTON,
        TOGGLE
    }
}
