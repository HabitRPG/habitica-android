package com.habitrpg.wearos.habitica.ui.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.wearos.habitica.databinding.RowHubBinding
import com.habitrpg.wearos.habitica.ui.activities.MenuItem

class HubAdapter: RecyclerView.Adapter<HubViewHolder>() {
    var data: List<MenuItem> = listOf()
    set(value) {
        field = value
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HubViewHolder {
        return HubViewHolder(RowHubBinding.inflate(parent.context.layoutInflater, parent, false).root)
    }

    override fun onBindViewHolder(holder: HubViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }
}

class HubViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val binding = RowHubBinding.bind(itemView)

    fun bind(item: MenuItem) {
        binding.title.text = item.title
        binding.iconView.setImageDrawable(item.icon)
        binding.root.setOnClickListener {
            item.onClick()
        }
    }
}