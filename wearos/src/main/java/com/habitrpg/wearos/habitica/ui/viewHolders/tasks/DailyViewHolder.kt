package com.habitrpg.wearos.habitica.ui.viewHolders.tasks

import android.content.res.ColorStateList
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.habitrpg.wearos.habitica.databinding.RowDailyBinding
import com.habitrpg.wearos.habitica.models.tasks.Task

class DailyViewHolder(itemView: View) : TaskViewHolder(itemView) {
    private val binding = RowDailyBinding.bind(itemView)
    override val titleView: TextView
        get() = binding.title

    init {
        binding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            onTaskScore?.invoke()
        }
    }

    override fun bind(data: Task) {
        super.bind(data)
        binding.checkbox.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(itemView.context, data.mediumTaskColor))
    }
}