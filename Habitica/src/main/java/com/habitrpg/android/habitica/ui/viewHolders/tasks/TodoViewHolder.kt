package com.habitrpg.android.habitica.ui.viewHolders.tasks

import android.view.View
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.formatForLocale
import com.habitrpg.android.habitica.helpers.GroupPlanInfoProvider
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.shared.habitica.models.responses.TaskDirection

class TodoViewHolder(
    itemView: View,
    scoreTaskFunc: ((Task, TaskDirection) -> Unit),
    scoreChecklistItemFunc: ((Task, ChecklistItem) -> Unit),
    openTaskFunc: ((Task, View) -> Unit),
    brokenTaskFunc: ((Task) -> Unit),
    assignedTextProvider: GroupPlanInfoProvider?,
) : ChecklistedViewHolder(itemView, scoreTaskFunc, scoreChecklistItemFunc, openTaskFunc, brokenTaskFunc, assignedTextProvider) {
    override fun bind(
        data: Task,
        position: Int,
        displayMode: String,
        ownerID: String?,
    ) {
        this.task = data
        setChecklistIndicatorBackgroundActive(data.isChecklistDisplayActive)
        reminderTextView.visibility = View.GONE
        this.streakTextView.visibility = View.GONE
        super.bind(data, position, displayMode, ownerID)
    }

    override fun configureSpecialTaskTextView(task: Task) {
        super.configureSpecialTaskTextView(task)
        if (task.dueDate != null) {
            if (task.isDueToday() == true) {
                specialTaskTextView?.text = context.getString(R.string.today)
            } else if (task.isDayOrMorePastDue() == true) {
                task.dueDate?.let { specialTaskTextView?.text = it.formatForLocale() }
                specialTaskTextView?.setTextColor(ContextCompat.getColor(context, R.color.maroon100_red100))
            } else {
                task.dueDate?.let { specialTaskTextView?.text = it.formatForLocale() }
                specialTaskTextView?.setTextColor(ContextCompat.getColor(context, R.color.gray_300))
            }
            this.specialTaskTextView?.visibility = View.VISIBLE
            calendarIconView?.visibility = View.VISIBLE
        } else {
            this.specialTaskTextView?.visibility = View.INVISIBLE
        }
    }

    override fun shouldDisplayAsActive(
        task: Task?,
        userID: String?,
    ): Boolean {
        return task?.completed(userID) != true
    }
}
