package com.habitrpg.wearos.habitica.ui.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.wearos.habitica.databinding.RowHabitBinding
import com.habitrpg.wearos.habitica.models.tasks.Task

class TaskListAdapter: RecyclerView.Adapter<TaskViewHolder>() {
    var data: List<Task> = listOf()
    set(value) {
        field = value
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(RowHabitBinding.inflate(parent.context.layoutInflater, parent, false).root)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }
}

class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val binding = RowHabitBinding.bind(itemView)

    fun bind(task: Task) {
        binding.title.text = task.text
    }
}
