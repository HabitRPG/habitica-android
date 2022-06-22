package com.habitrpg.wearos.habitica.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.databinding.RowHeaderBinding
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.wearos.habitica.models.tasks.Task
import com.habitrpg.wearos.habitica.ui.viewHolders.HeaderViewHolder
import com.habitrpg.wearos.habitica.ui.viewHolders.tasks.TaskViewHolder

open class TaskListAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var title: String = ""
    var onTaskScore: ((Task) -> Unit)? = null
    var onTaskTapped:((Task) -> Unit)? = null
    var data: List<Task> = listOf()
    set(value) {
        field = value
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = parent.context.layoutInflater
        return HeaderViewHolder(RowHeaderBinding.inflate(inflater, parent, false).root)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TaskViewHolder) {
            val item = data[position - 1]
            holder.bind(item)
            holder.onTaskScore = {
                onTaskScore?.invoke(item)
            }
            holder.itemView.setOnClickListener {
                onTaskTapped?.invoke(item)
            }
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