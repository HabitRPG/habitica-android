package com.habitrpg.wearos.habitica.ui.viewHolders.tasks

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.habitrpg.android.habitica.databinding.RowTodoBinding

class ToDoViewHolder(itemView: View) : CheckedTaskViewHolder(itemView) {
    private val binding = RowTodoBinding.bind(itemView)
    override val titleView: TextView
        get() = binding.title
    override val checkbox: ImageView
        get() = binding.checkbox
    override val checkboxWrapper: ViewGroup
        get() = binding.checkboxWrapper
}