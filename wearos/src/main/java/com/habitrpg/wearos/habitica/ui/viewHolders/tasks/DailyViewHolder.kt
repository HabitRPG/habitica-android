package com.habitrpg.wearos.habitica.ui.viewHolders.tasks

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.habitrpg.android.habitica.databinding.RowDailyBinding
import com.habitrpg.wearos.habitica.models.tasks.Task

class DailyViewHolder(itemView: View) : CheckedTaskViewHolder(itemView) {
    private val binding = RowDailyBinding.bind(itemView)
    override val titleView: TextView
        get() = binding.title
    override val checkbox: ImageView
        get() = binding.checkbox
    override val checkboxWrapper: ViewGroup
        get() = binding.checkboxWrapper

    override fun bind(data: Task) {
        super.bind(data)
        val streakString = data.streakString
        if (streakString?.isNotBlank() == true) {
            binding.streakView.text = streakString
            binding.streakView.isVisible = true
        } else {
            binding.streakView.isVisible = false
        }
    }
}