package com.habitrpg.wearos.habitica.ui.viewHolders.tasks

import android.view.View
import android.widget.TextView
import com.habitrpg.common.habitica.models.responses.TaskDirection
import com.habitrpg.wearos.habitica.models.tasks.Task
import com.habitrpg.wearos.habitica.ui.viewHolders.BindableViewHolder

abstract class TaskViewHolder(itemView: View) : BindableViewHolder<Task>(itemView) {
    var onTaskScored: ((TaskDirection) -> Unit)? = null
    abstract val titleView: TextView
    override fun bind(data: Task) {
        titleView.text = data.text
    }
}