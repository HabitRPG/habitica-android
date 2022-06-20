package com.habitrpg.wearos.habitica.ui.viewHolders.tasks

import android.content.res.ColorStateList
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.databinding.RowTodoBinding
import com.habitrpg.wearos.habitica.models.tasks.Task

class ToDoViewHolder(itemView: View) : TaskViewHolder(itemView) {
    private val binding = RowTodoBinding.bind(itemView)
    override val titleView: TextView
        get() = binding.title

    init {
        binding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            onTaskScore?.invoke()
        }
    }

    override fun bind(data: Task) {
        super.bind(data)
        binding.checkbox.isChecked = data.completed
        binding.checkbox.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(itemView.context, data.mediumTaskColor))
    }
}