package com.habitrpg.wearos.habitica.ui.viewHolders.tasks

import android.content.res.ColorStateList
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.databinding.RowHabitBinding
import com.habitrpg.wearos.habitica.models.tasks.Task

class HabitViewHolder(itemView: View) : TaskViewHolder(itemView) {
    private val binding = RowHabitBinding.bind(itemView)
    override val titleView: TextView
        get() = binding.title

    init {
        binding.habitButton.setOnClickListener {
            onTaskScore?.invoke()
        }
    }

    override fun bind(data: Task) {
        super.bind(data)
        binding.habitButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(itemView.context, data.mediumTaskColor))
    }
}