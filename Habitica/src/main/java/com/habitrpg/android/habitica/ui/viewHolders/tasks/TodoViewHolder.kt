package com.habitrpg.android.habitica.ui.viewHolders.tasks

import android.view.View
import com.habitrpg.shared.habitica.models.responses.TaskDirection
import com.habitrpg.shared.habitica.models.tasks.ChecklistItem

import com.habitrpg.shared.habitica.models.tasks.Task

import java.text.DateFormat

class TodoViewHolder(itemView: View, scoreTaskFunc: ((Task, TaskDirection) -> Unit), scoreChecklistItemFunc: ((Task, ChecklistItem) -> Unit), openTaskFunc: ((Task) -> Unit)) : ChecklistedViewHolder(itemView, scoreTaskFunc, scoreChecklistItemFunc, openTaskFunc) {

    private val dateFormatter: DateFormat = android.text.format.DateFormat.getDateFormat(context)

    override fun bind(newTask: Task, position: Int, displayMode: String) {
        this.task = newTask
        if (newTask.completed) {
            checklistIndicatorWrapper.setBackgroundColor(taskGray)
        } else {
            checklistIndicatorWrapper.setBackgroundColor(data.lightTaskColor)
        }
        super.bind(newTask, position, displayMode)
    }

    override fun configureSpecialTaskTextView(task: Task) {
        val taskDueDate = task.dueDate
        if (taskDueDate != null) {
            this.specialTaskTextView?.text = dateFormatter.format(taskDueDate)
            this.specialTaskTextView?.visibility = View.VISIBLE
        } else {
            this.specialTaskTextView?.visibility = View.INVISIBLE
        }
    }

    override fun shouldDisplayAsActive(newTask: Task): Boolean {
        return !newTask.completed
    }
}
