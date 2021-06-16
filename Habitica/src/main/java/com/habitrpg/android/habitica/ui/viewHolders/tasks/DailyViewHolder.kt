package com.habitrpg.android.habitica.ui.viewHolders.tasks

import android.view.View
import com.habitrpg.android.habitica.models.responses.TaskDirection
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.Task
import java.text.DateFormat
import java.util.*

class DailyViewHolder(itemView: View, scoreTaskFunc: ((Task, TaskDirection) -> Unit), scoreChecklistItemFunc: ((Task, ChecklistItem) -> Unit), openTaskFunc: ((Task) -> Unit), brokenTaskFunc: ((Task) -> Unit)) : ChecklistedViewHolder(itemView, scoreTaskFunc, scoreChecklistItemFunc, openTaskFunc, brokenTaskFunc) {

    override val taskIconWrapperIsVisible: Boolean
        get() {
            var isVisible: Boolean = super.taskIconWrapperIsVisible
            if (this.streakTextView.visibility == View.VISIBLE) {
                isVisible = true
            }
            return isVisible
        }

    override fun bind(data: Task, position: Int, displayMode: String) {
        this.task = data
        setChecklistIndicatorBackgroundActive(data.isChecklistDisplayActive)

        if (data.reminders?.size == 0) {
            reminderTextView.visibility = View.GONE
        } else {
            reminderTextView.visibility = View.VISIBLE
            val now = Date()
            val calendar = Calendar.getInstance()
            val nextReminder = data.reminders?.firstOrNull {
                calendar.time = now
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), it.time?.hours ?: 0, it.time?.minutes ?: 0, 0)
                now < calendar.time
            } ?: data.reminders?.first()

            var reminderString = ""
            if (nextReminder?.time != null) {
                reminderString += formatter.format(nextReminder.time)
            }
            if ((data.reminders?.size ?: 0) > 1) {
                reminderString = "$reminderString (+${(data.reminders?.size ?: 0)-1})"
            }
            reminderTextView.text = reminderString
        }

        super.bind(data, position, displayMode)
    }

    override fun shouldDisplayAsActive(newTask: Task?): Boolean {
        return newTask?.isDisplayedActive ?: false
    }

    override fun configureSpecialTaskTextView(task: Task) {
        super.configureSpecialTaskTextView(task)
        if (task.streak ?: 0 > 0) {
            this.streakTextView.text = task.streak.toString()
            this.streakTextView.visibility = View.VISIBLE
            this.streakIconView.visibility = View.VISIBLE
        } else {
            this.streakTextView.visibility = View.GONE
            this.streakIconView.visibility = View.GONE
        }
    }

    companion object {
        private val formatter: DateFormat
            get() {
                return DateFormat.getTimeInstance(DateFormat.SHORT)
            }
    }
}
