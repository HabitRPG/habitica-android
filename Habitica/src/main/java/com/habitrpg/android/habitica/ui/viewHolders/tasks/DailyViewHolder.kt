package com.habitrpg.android.habitica.ui.viewHolders.tasks

import android.view.View
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.shared.habitica.models.responses.TaskDirection
import com.habitrpg.shared.habitica.models.tasks.ChecklistItem
import com.habitrpg.shared.habitica.models.tasks.Task
import java.text.DateFormat
import java.util.*

class DailyViewHolder(itemView: View, scoreTaskFunc: ((Task, TaskDirection) -> Unit), scoreChecklistItemFunc: ((Task, ChecklistItem) -> Unit), openTaskFunc: ((Task) -> Unit)) : ChecklistedViewHolder(itemView, scoreTaskFunc, scoreChecklistItemFunc, openTaskFunc) {

    private val streakTextView: TextView by bindView(itemView, R.id.streakTextView)
    private val reminderTextView: TextView by bindView(itemView, R.id.reminder_textview)


    override val taskIconWrapperIsVisible: Boolean
        get() {
            var isVisible: Boolean = super.taskIconWrapperIsVisible
            if (this.streakTextView.visibility == View.VISIBLE) {
                isVisible = true
            }
            return isVisible
        }

    override fun bind(data: Task, position: Int) {
        this.task = data
        if (data.isChecklistDisplayActive) {
            this.checklistIndicatorWrapper.setBackgroundResource(data.lightTaskColor)
        } else {
            this.checklistIndicatorWrapper.setBackgroundColor(this.taskGray)
        }

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
            val reminderTime = nextReminder?.time
            if (reminderTime != null) {
                reminderString += formatter.format(reminderTime)
            }
            if ((data.reminders?.size ?: 0) > 1) {
                reminderString = "$reminderString (+${(data.reminders?.size ?: 0)-1})"
            }
            reminderTextView.text = reminderString
        }

        super.bind(data, position)
    }

    override fun shouldDisplayAsActive(newTask: Task): Boolean {
        return newTask.isDisplayedActive
    }

    override fun configureSpecialTaskTextView(task: Task) {
        super.configureSpecialTaskTextView(task)
        if (task.streak ?: 0 > 0) {
            this.streakTextView.text = task.streak.toString()
            this.streakTextView.visibility = View.VISIBLE
        } else {
            this.streakTextView.visibility = View.GONE
        }
    }

    companion object {
        private val formatter: DateFormat
            get() {
                return DateFormat.getTimeInstance(DateFormat.SHORT)
            }
    }
}
