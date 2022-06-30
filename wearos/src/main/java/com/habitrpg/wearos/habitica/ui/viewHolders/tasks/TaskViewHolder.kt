package com.habitrpg.wearos.habitica.ui.viewHolders.tasks

import android.view.View
import com.habitrpg.wearos.habitica.models.tasks.Task
import com.habitrpg.wearos.habitica.ui.viewHolders.BindableViewHolder
import com.habitrpg.wearos.habitica.ui.views.TaskTextView

abstract class TaskViewHolder(itemView: View) : BindableViewHolder<Task>(itemView) {
    var onTaskScore: (() -> Unit)? = null
    abstract val titleView: TaskTextView
    override fun bind(data: Task) {
        titleView.text = data.text
    }
}