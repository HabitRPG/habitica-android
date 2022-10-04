package com.habitrpg.android.habitica.ui.viewHolders.tasks

import android.graphics.PorterDuff
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.GroupPlanInfoProvider
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.shared.habitica.models.responses.TaskDirection

class HabitViewHolder(
    itemView: View,
    scoreTaskFunc: ((Task, TaskDirection) -> Unit),
    openTaskFunc: ((Pair<Task, View>) -> Unit),
    brokenTaskFunc: ((Task) -> Unit),
    assignedTextProvider: GroupPlanInfoProvider?
) : BaseTaskViewHolder(itemView, scoreTaskFunc, openTaskFunc, brokenTaskFunc, assignedTextProvider) {

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
        btnPlus.isClickable = true
        btnMinus.setOnClickListener { onMinusButtonClicked() }
        btnMinus.isClickable = true
    }

    override fun bind(
        data: Task,
        position: Int,
        displayMode: String,
        ownerID: String?
    ) {
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

        val streakString = task?.streakString
        if (streakString?.isNotEmpty() == true) {
            streakTextView.text = streakString
            streakTextView.visibility = View.VISIBLE
            streakIconView.visibility = View.VISIBLE
        } else {
            streakTextView.visibility = View.GONE
            streakIconView.visibility = View.GONE
        }
        reminderTextView.visibility = View.GONE
        calendarIconView?.visibility = View.GONE
        super.bind(data, position, displayMode, ownerID)
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
