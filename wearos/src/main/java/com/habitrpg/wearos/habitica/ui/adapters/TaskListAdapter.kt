package com.habitrpg.wearos.habitica.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.databinding.RowSectionHeaderBinding
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.wearos.habitica.models.tasks.Task
import com.habitrpg.wearos.habitica.ui.viewHolders.HeaderSectionViewHolder
import com.habitrpg.wearos.habitica.ui.viewHolders.tasks.TaskViewHolder

open class TaskListAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var title: String = ""
    var onTaskScore: ((Task) -> Unit)? = null
    var onTaskTapped:((Task) -> Unit)? = null
    var onRefresh:(() -> Unit)? = null
    var data: List<Any> = listOf()
    set(value) {
        field = value
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = parent.context.layoutInflater
        return HeaderSectionViewHolder(RowSectionHeaderBinding.inflate(inflater, parent, false).root)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TaskViewHolder) {
            val item = data[position - 1] as Task
            holder.bind(item)
            holder.onTaskScore = {
                onTaskScore?.invoke(item)
            }
            holder.itemView.setOnClickListener {
                onTaskTapped?.invoke(item)
            }
        } else if (holder is HeaderSectionViewHolder){
            if (position == 0) {
                holder.bind(title)
                holder.itemView.setOnClickListener {
                    onRefresh?.invoke()
                }
            } else {
                holder.bind(data[position - 1] as String)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            0
        } else {
            val item = data[position - 1]
            if (item is Task)  1 else 0
        }
    }

    override fun getItemCount(): Int {
        return data.size + 1
    }
}