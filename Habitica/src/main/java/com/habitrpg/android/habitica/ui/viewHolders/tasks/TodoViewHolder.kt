package com.habitrpg.android.habitica.ui.viewHolders.tasks

import android.view.View
import com.habitrpg.common.habitica.models.responses.TaskDirection
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.Task
import java.text.DateFormat

class TodoViewHolder(
    itemView: View,
    scoreTaskFunc: ((Task, TaskDirection) -> Unit),
    scoreChecklistItemFunc: ((Task, ChecklistItem) -> Unit),
    openTaskFunc: ((Pair<Task, View>) -> Unit),
    brokenTaskFunc: ((Task) -> Unit)
) : ChecklistedViewHolder(itemView, scoreTaskFunc, scoreChecklistItemFunc, openTaskFunc, brokenTaskFunc) {

    private val dateFormatter: DateFormat = android.text.format.DateFormat.getDateFormat(context)

    override fun bind(data: Task, position: Int, displayMode: String) {
        this.task = data
        setChecklistIndicatorBackgroundActive(data.isChecklistDisplayActive)
        reminderTextView.visibility = View.GONE
        this.streakTextView.visibility = View.GONE
        super.bind(data, position, displayMode)
    }

    override fun configureSpecialTaskTextView(task: Task) {
        super.configureSpecialTaskTextView(task)
        if (task.dueDate != null) {
            task.dueDate?.let { specialTaskTextView?.text = dateFormatter.format(it) }
            this.specialTaskTextView?.visibility = View.VISIBLE
            calendarIconView?.visibility = View.VISIBLE
        } else {
            this.specialTaskTextView?.visibility = View.INVISIBLE
        }
    }

    override fun shouldDisplayAsActive(newTask: Task?): Boolean {
        return newTask?.completed != true
    }
}
