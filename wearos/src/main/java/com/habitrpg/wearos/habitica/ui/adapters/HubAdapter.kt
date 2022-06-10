package com.habitrpg.wearos.habitica.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.databinding.RowHeaderBinding
import com.habitrpg.android.habitica.databinding.RowHubBinding
import com.habitrpg.wearos.habitica.models.MenuItem
import com.habitrpg.wearos.habitica.ui.viewHolders.HeaderViewHolder
import com.habitrpg.wearos.habitica.ui.viewHolders.HubViewHolder

class HubAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var title: String = ""
    var data: List<MenuItem> = listOf()
    set(value) {
        field = value
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = parent.context.layoutInflater
        return if (viewType == 0) {
            HeaderViewHolder(RowHeaderBinding.inflate(inflater, parent, false).root)
        } else {
            HubViewHolder(RowHubBinding.inflate(inflater, parent, false).root)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HubViewHolder) {
            val item = data[position - 1]
            holder.bind(item)
        } else if (holder is HeaderViewHolder){
            holder.bind(title)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) 0 else 1
    }

    override fun getItemCount(): Int {
        return data.size + 1
    }
}

