package com.habitrpg.android.habitica.ui.viewHolders.tasks

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.setTintWith
import com.habitrpg.android.habitica.models.responses.TaskDirection
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper

class HabitViewHolder(itemView: View, scoreTaskFunc: ((Task, TaskDirection) -> Unit), openTaskFunc: ((Task) -> Unit), brokenTaskFunc: ((Task) -> Unit)) : BaseTaskViewHolder(itemView, scoreTaskFunc, openTaskFunc, brokenTaskFunc) {

    private val btnPlusWrapper: FrameLayout = itemView.findViewById(R.id.btnPlusWrapper)
    private val btnPlusIconView: ImageView = itemView.findViewById(R.id.btnPlusIconView)
    private val btnPlusCircleView: View = itemView.findViewById(R.id.button_plus_circle_view)
    private val btnPlus: Button = itemView.findViewById(R.id.btnPlus)
    private val btnMinusWrapper: FrameLayout = itemView.findViewById(R.id.btnMinusWrapper)
    private val btnMinusIconView: ImageView = itemView.findViewById(R.id.btnMinusIconView)
    private val btnMinusCircleView: View = itemView.findViewById(R.id.button_minus_circle_view)
    private val btnMinus: Button = itemView.findViewById(R.id.btnMinus)

    init {
        btnPlus.setOnClickListener { onPlusButtonClicked() }
        btnMinus.setOnClickListener { onMinusButtonClicked() }
    }

    override fun bind(data: Task, position: Int, displayMode: String) {
        this.task = data
        if (data.up == true) {
            val plusIcon = if (isLocked) {
                val icon = ContextCompat.getDrawable(context, R.drawable.task_lock)
                icon?.setTint(ContextCompat.getColor(context, data.darkestTaskColor))
                icon
            } else {
                val icon = ContextCompat.getDrawable(context, R.drawable.habit_plus)
                icon?.setTint(ContextCompat.getColor(context, R.color.white))
                icon
            }
            plusIcon?.setTintMode(PorterDuff.Mode.MULTIPLY)
            this.btnPlusIconView.setImageDrawable(plusIcon)
            val drawable = ContextCompat.getDrawable(context, R.drawable.habit_circle)
            this.btnPlusWrapper.setBackgroundResource(data.lightTaskColor)
            drawable?.setTint(ContextCompat.getColor(context, data.mediumTaskColor))
            drawable?.setTintMode(PorterDuff.Mode.MULTIPLY)
            btnPlusCircleView.background = drawable
            this.btnPlus.visibility = View.VISIBLE
            this.btnPlus.isClickable = true
        } else {
            this.btnPlusWrapper.setBackgroundResource(R.color.habit_inactive_gray)
            val plusIcon = if (isLocked) {
                val icon = ContextCompat.getDrawable(context, R.drawable.task_lock)
                icon?.setTint(ContextCompat.getColor(context, R.color.text_dimmed))
                icon
            } else {
                val icon = ContextCompat.getDrawable(context, R.drawable.habit_plus)
                icon?.setTint(ContextCompat.getColor(context, R.color.content_background_offset))
                icon
            }
            plusIcon?.setTintMode(PorterDuff.Mode.MULTIPLY)
            this.btnPlusIconView.setImageDrawable(plusIcon)
            btnPlusCircleView.background = ContextCompat.getDrawable(context, R.drawable.habit_circle_disabled)
            this.btnPlus.visibility = View.GONE
            this.btnPlus.isClickable = false
        }

        if (data.down == true) {
            this.btnMinusWrapper.setBackgroundResource(data.lightTaskColor)
            val minusIcon = if (isLocked) {
                val icon = ContextCompat.getDrawable(context, R.drawable.task_lock)
                icon?.setTint(ContextCompat.getColor(context, data.darkestTaskColor))
                icon
            } else {
                val icon = ContextCompat.getDrawable(context, R.drawable.habit_minus)
                icon?.setTint(ContextCompat.getColor(context, R.color.white))
                icon
            }
            minusIcon?.setTintMode(PorterDuff.Mode.MULTIPLY)
            this.btnMinusIconView.setImageDrawable(minusIcon)
            val drawable = ContextCompat.getDrawable(context, R.drawable.habit_circle)
            this.btnMinusWrapper.setBackgroundResource(data.lightTaskColor)
            drawable?.setTint(ContextCompat.getColor(context, data.mediumTaskColor))
            drawable?.setTintMode(PorterDuff.Mode.MULTIPLY)
            btnMinusCircleView.background = drawable
            this.btnMinus.visibility = View.VISIBLE
            this.btnMinus.isClickable = true
        } else {
            this.btnMinusWrapper.setBackgroundResource(R.color.habit_inactive_gray)
            val minusIcon = if (isLocked) {
                val icon = ContextCompat.getDrawable(context, R.drawable.task_lock)
                icon?.setTint(ContextCompat.getColor(context, R.color.content_background_offset))
                icon
            } else {
                val icon = ContextCompat.getDrawable(context, R.drawable.habit_minus)
                icon?.setTint(ContextCompat.getColor(context, R.color.content_background_offset))
                icon
            }
            minusIcon?.setTintMode(PorterDuff.Mode.MULTIPLY)
            this.btnMinusIconView.setImageDrawable(minusIcon)
            btnMinusCircleView.background = ContextCompat.getDrawable(context, R.drawable.habit_circle_disabled)
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
            streakIconView.visibility = View.VISIBLE
        } else {
            streakTextView.visibility = View.GONE
            streakIconView.visibility = View.GONE
        }
        reminderTextView.visibility = View.GONE
        calendarIconView?.visibility = View.GONE
        super.bind(data, position, displayMode)
        if (data.up == false && data.down == false) {
            titleTextView.setTextColor(ContextCompat.getColor(context, R.color.text_quad))
            notesTextView?.setTextColor(ContextCompat.getColor(context, R.color.text_quad))
        }
    }

    override fun onLeftActionTouched() {
        super.onLeftActionTouched()
        onPlusButtonClicked()
    }

    override fun onRightActionTouched() {
        super.onRightActionTouched()
        onMinusButtonClicked()
    }

    private fun onPlusButtonClicked() {
        if (task?.up != true) return
        task?.let { scoreTaskFunc.invoke(it, TaskDirection.UP) }
    }

    private fun onMinusButtonClicked() {
        if (task?.down != true) return
        task?.let { scoreTaskFunc.invoke(it, TaskDirection.DOWN) }
    }

    override fun setDisabled(openTaskDisabled: Boolean, taskActionsDisabled: Boolean) {
        super.setDisabled(openTaskDisabled, taskActionsDisabled)

        this.btnPlus.isEnabled = !taskActionsDisabled
        this.btnMinus.isEnabled = !taskActionsDisabled
    }
}
