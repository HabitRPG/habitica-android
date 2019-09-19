package com.habitrpg.android.habitica.ui.viewHolders.tasks

import android.view.View
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.responses.TaskDirection
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.helpers.bindView
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

    override fun bind(newTask: Task, position: Int) {
        this.task = newTask
        if (newTask.isChecklistDisplayActive) {
            this.checklistIndicatorWrapper.setBackgroundResource(newTask.lightTaskColor)
        } else {
            this.checklistIndicatorWrapper.setBackgroundColor(this.taskGray)
        }

        if (newTask.reminders?.size == 0) {
            reminderTextView.visibility = View.GONE
        } else {
            reminderTextView.visibility = View.VISIBLE
            val now = Date()
            val calendar = Calendar.getInstance()
            val nextReminder = newTask.reminders?.firstOrNull {
                calendar.time = now
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), it.time?.hours ?: 0, it.time?.minutes ?: 0, 0)
                now < calendar.time
            } ?: newTask.reminders?.first()

            var reminderString = ""
            if (nextReminder?.time != null) {
                reminderString += formatter.format(nextReminder.time)
            }
            if ((newTask.reminders?.size ?: 0) > 1) {
                reminderString = "$reminderString (+${(newTask.reminders?.size ?: 0)-1})"
            }
            reminderTextView.text = reminderString
        }

        super.bind(newTask, position)
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
