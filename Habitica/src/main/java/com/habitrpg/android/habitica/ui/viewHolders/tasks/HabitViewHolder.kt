package com.habitrpg.android.habitica.ui.viewHolders.tasks

import android.graphics.PorterDuff
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.isUsingNightModeResources
import com.habitrpg.android.habitica.models.responses.TaskDirection
import com.habitrpg.android.habitica.models.tasks.Task

class HabitViewHolder(itemView: View, scoreTaskFunc: ((Task, TaskDirection) -> Unit), openTaskFunc: ((Task) -> Unit), brokenTaskFunc: ((Task) -> Unit)) : BaseTaskViewHolder(itemView, scoreTaskFunc, openTaskFunc, brokenTaskFunc) {

    private val btnPlusWrapper: FrameLayout = itemView.findViewById(R.id.btnPlusWrapper)
    private val btnPlusIconView: ImageView = itemView.findViewById(R.id.btnPlusIconView)
    private val btnPlus: Button = itemView.findViewById(R.id.btnPlus)
    private val btnMinusWrapper: FrameLayout = itemView.findViewById(R.id.btnMinusWrapper)
    private val btnMinusIconView: ImageView = itemView.findViewById(R.id.btnMinusIconView)
    private val btnMinus: Button = itemView.findViewById(R.id.btnMinus)

    init {
        btnPlus.setOnClickListener { onPlusButtonClicked() }
        btnMinus.setOnClickListener { onMinusButtonClicked() }
    }

    override fun bind(data: Task, position: Int, displayMode: String) {
        this.task = data
        if (data.up == true) {
            val plusIcon = ContextCompat.getDrawable(context, R.drawable.habit_plus)
            plusIcon?.setTint(ContextCompat.getColor(context, R.color.white))
            plusIcon?.setTintMode(PorterDuff.Mode.MULTIPLY)
            this.btnPlusIconView.setImageDrawable(plusIcon)
            val drawable = ContextCompat.getDrawable(context, R.drawable.habit_circle)
            if (context.isUsingNightModeResources()) {
                this.btnPlusWrapper.setBackgroundResource(data.mediumTaskColor)
                drawable?.setTint(ContextCompat.getColor(context, data.darkTaskColor))
            } else {
                this.btnPlusWrapper.setBackgroundResource(data.lightTaskColor)
                drawable?.setTint(ContextCompat.getColor(context, data.mediumTaskColor))
            }
            drawable?.setTintMode(PorterDuff.Mode.MULTIPLY)
            btnPlusIconView.background = drawable
            this.btnPlus.visibility = View.VISIBLE
            this.btnPlus.isClickable = true
        } else {
            this.btnPlusWrapper.setBackgroundResource(R.color.habit_inactive_gray)
            val plusIcon = ContextCompat.getDrawable(context, R.drawable.habit_plus)
            plusIcon?.setTint(ContextCompat.getColor(context, R.color.content_background_offset))
            plusIcon?.setTintMode(PorterDuff.Mode.MULTIPLY)
            this.btnPlusIconView.setImageDrawable(plusIcon)
            btnPlusIconView.background = ContextCompat.getDrawable(context, R.drawable.habit_circle_disabled)
            this.btnPlus.visibility = View.GONE
            this.btnPlus.isClickable = false
        }

        if (data.down == true) {
            this.btnMinusWrapper.setBackgroundResource(data.lightTaskColor)
            val minusIcon = ContextCompat.getDrawable(context, R.drawable.habit_minus)
            minusIcon?.setTint(ContextCompat.getColor(context, R.color.white))
            minusIcon?.setTintMode(PorterDuff.Mode.MULTIPLY)
            this.btnMinusIconView.setImageDrawable(minusIcon)
            val drawable = ContextCompat.getDrawable(context, R.drawable.habit_circle)
            if (context.isUsingNightModeResources()) {
                this.btnMinusWrapper.setBackgroundResource(data.mediumTaskColor)
                drawable?.setTint(ContextCompat.getColor(context, data.darkTaskColor))
            } else {
                this.btnMinusWrapper.setBackgroundResource(data.lightTaskColor)
                drawable?.setTint(ContextCompat.getColor(context, data.mediumTaskColor))
            }
            drawable?.setTintMode(PorterDuff.Mode.MULTIPLY)
            btnMinusIconView.background = drawable
            this.btnMinus.visibility = View.VISIBLE
            this.btnMinus.isClickable = true
        } else {
            this.btnMinusWrapper.setBackgroundResource(R.color.habit_inactive_gray)
            val minusIcon = ContextCompat.getDrawable(context, R.drawable.habit_minus)
            minusIcon?.setTint(ContextCompat.getColor(context, R.color.content_background_offset))
            minusIcon?.setTintMode(PorterDuff.Mode.MULTIPLY)
            this.btnMinusIconView.setImageDrawable(minusIcon)
            btnMinusIconView.background = ContextCompat.getDrawable(context, R.drawable.habit_circle_disabled)
            this.btnMinus.visibility = View.GONE
            this.btnMinus.isClickable = false
        }

        var streakString = ""
        if (data.counterUp != null && data.counterUp ?: 0 > 0 && data.counterDown != null && data.counterDown ?: 0 > 0) {
            streakString = streakString + "+" + data.counterUp.toString() + " | -" + data.counterDown?.toString()
        } else if (data.counterUp != null && data.counterUp ?: 0 > 0) {
            streakString = streakString + "+" + data.counterUp.toString()
        } else if (data.counterDown != null && data.counterDown ?: 0 > 0) {
            streakString = streakString + "-" + data.counterDown.toString()
        }
        if (streakString.isNotEmpty()) {
            streakTextView.text = streakString
            streakTextView.visibility = View.VISIBLE
        } else {
            streakTextView.visibility = View.GONE
        }
        reminderTextView.visibility = View.GONE
        calendarIconView?.visibility = View.GONE
        super.bind(data, position, displayMode)
    }

    private fun onPlusButtonClicked() {
        task?.let { scoreTaskFunc.invoke(it, TaskDirection.UP) }
    }

    private fun onMinusButtonClicked() {
        task?.let { scoreTaskFunc.invoke(it, TaskDirection.DOWN) }
    }

    override fun setDisabled(openTaskDisabled: Boolean, taskActionsDisabled: Boolean) {
        super.setDisabled(openTaskDisabled, taskActionsDisabled)

        this.btnPlus.isEnabled = !taskActionsDisabled
        this.btnMinus.isEnabled = !taskActionsDisabled
    }
}
