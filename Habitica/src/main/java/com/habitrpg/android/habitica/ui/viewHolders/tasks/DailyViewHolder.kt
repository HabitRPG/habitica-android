package com.habitrpg.android.habitica.ui.viewHolders.tasks

import android.view.View
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.models.tasks.Task

class DailyViewHolder(itemView: View) : ChecklistedViewHolder(itemView) {

    private val streakTextView: TextView by bindView(itemView, R.id.streakTextView)

    override val taskIconWrapperIsVisible: Boolean
        get() {
            var isVisible: Boolean = super.taskIconWrapperIsVisible
            if (this.streakTextView.visibility == View.VISIBLE) {
                isVisible = true
            }
            return isVisible
        }

    override fun bindHolder(newTask: Task, position: Int) {
        this.task = newTask
        if (newTask.isChecklistDisplayActive) {
            this.checklistIndicatorWrapper.setBackgroundResource(newTask.lightTaskColor)
        } else {
            this.checklistIndicatorWrapper.setBackgroundColor(this.taskGray)
        }
        super.bindHolder(newTask, position)
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
}
