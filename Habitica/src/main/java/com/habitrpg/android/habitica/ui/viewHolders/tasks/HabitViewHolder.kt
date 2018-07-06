package com.habitrpg.android.habitica.ui.viewHolders.tasks

import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import butterknife.OnClick
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.events.HabitScoreEvent
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.models.tasks.Task
import org.greenrobot.eventbus.EventBus

class HabitViewHolder(itemView: View) : BaseTaskViewHolder(itemView) {

    private val btnPlusWrapper: FrameLayout by bindView(itemView, R.id.btnPlusWrapper)
    private val btnPlusIconView: ImageView by bindView(itemView, R.id.btnPlusIconView)
    private val btnPlus: Button by bindView(itemView, R.id.btnPlus)
    private val btnMinusWrapper: FrameLayout by bindView(itemView, R.id.btnMinusWrapper)
    private val btnMinusIconView: ImageView by bindView(itemView, R.id.btnMinusIconView)
    private val btnMinus: Button by bindView(itemView, R.id.btnMinus)
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
        if (newTask.up == true) {
            this.btnPlusWrapper.setBackgroundResource(newTask.lightTaskColor)
            if (newTask.lightTaskColor == R.color.yellow_100) {
                this.btnPlusIconView.setImageResource(R.drawable.habit_plus_yellow)
            } else {
                this.btnPlusIconView.setImageResource(R.drawable.habit_plus)
            }
            this.btnPlus.visibility = View.VISIBLE
            this.btnPlus.isClickable = true
        } else {
            this.btnPlusWrapper.setBackgroundResource(R.color.habit_inactive_gray)
            this.btnPlusIconView.setImageResource(R.drawable.habit_plus_disabled)
            this.btnPlus.visibility = View.GONE
            this.btnPlus.isClickable = false
        }

        if (newTask.down == true) {
            this.btnMinusWrapper.setBackgroundResource(newTask.lightTaskColor)
            if (newTask.lightTaskColor == R.color.yellow_100) {
                this.btnMinusIconView.setImageResource(R.drawable.habit_minus_yellow)
            } else {
                this.btnMinusIconView.setImageResource(R.drawable.habit_minus)
            }
            this.btnMinus.visibility = View.VISIBLE
            this.btnMinus.isClickable = true
        } else {
            this.btnMinusWrapper.setBackgroundResource(R.color.habit_inactive_gray)
            this.btnMinusIconView.setImageResource(R.drawable.habit_minus_disabled)
            this.btnMinus.visibility = View.GONE
            this.btnMinus.isClickable = false
        }

        var streakString = ""
        if (newTask.counterUp != null && newTask.counterUp ?: 0 > 0 && newTask.counterDown != null && newTask.counterDown ?: 0 > 0) {
            streakString = streakString + "+" + newTask.counterUp.toString() + " | -" + newTask.counterDown?.toString()
        } else if (newTask.counterUp != null && newTask.counterUp ?: 0 > 0) {
            streakString = streakString + "+" + newTask.counterUp.toString()
        } else if (newTask.counterDown != null && newTask.counterDown ?: 0 > 0) {
            streakString = streakString + "-" + newTask.counterDown.toString()
        }
        if (streakString.isNotEmpty()) {
            streakTextView.text = streakString
            streakTextView.visibility = View.VISIBLE
        } else {
            streakTextView.visibility = View.GONE
        }
        super.bindHolder(newTask, position)
    }

    @OnClick(R.id.btnPlus)
    fun onPlusButtonClicked() {
        val event = HabitScoreEvent()
        event.Up = true
        event.habit = task
        EventBus.getDefault().post(event)
    }

    @OnClick(R.id.btnMinus)
    fun onMinusButtonClicked() {
        val event = HabitScoreEvent()
        event.Up = false
        event.habit = task
        EventBus.getDefault().post(event)
    }

    override fun setDisabled(openTaskDisabled: Boolean, taskActionsDisabled: Boolean) {
        super.setDisabled(openTaskDisabled, taskActionsDisabled)

        this.btnPlus.isEnabled = !taskActionsDisabled
        this.btnMinus.isEnabled = !taskActionsDisabled
    }
}
