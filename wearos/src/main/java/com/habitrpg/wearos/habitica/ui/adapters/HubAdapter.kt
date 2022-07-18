package com.habitrpg.wearos.habitica.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.databinding.RowHubBinding
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.wearos.habitica.models.user.MenuItem
import com.habitrpg.wearos.habitica.ui.viewHolders.HubViewHolder

class HubAdapter: BaseAdapter<MenuItem>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = parent.context.layoutInflater
        return if (viewType == 1) {
            HubViewHolder(RowHubBinding.inflate(inflater, parent, false).root)
        } else {
            super.onCreateViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HubViewHolder) {
            holder.bind(getItemAt(position))
        } else {
            super.onBindViewHolder(holder, position)
        }
    }

    override fun getItemViewType(position: Int) = if (position == 0) TYPE_HEADER else 1
}

