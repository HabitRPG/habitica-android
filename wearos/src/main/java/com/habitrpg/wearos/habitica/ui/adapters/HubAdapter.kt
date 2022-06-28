package com.habitrpg.wearos.habitica.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.databinding.RowHeaderBinding
import com.habitrpg.android.habitica.databinding.RowHubBinding
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.wearos.habitica.models.user.MenuItem
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
            holder.bind(getItemAt(position - 1))
        } else if (holder is HeaderViewHolder){
            holder.bind(title)
        }
    }

    private fun getItemAt(position: Int) = data.filter { !it.isHidden }[position]
    override fun getItemViewType(position: Int) = if (position == 0) 0 else 1
    override fun getItemCount() =  data.filter { !it.isHidden }.size + 1
}

