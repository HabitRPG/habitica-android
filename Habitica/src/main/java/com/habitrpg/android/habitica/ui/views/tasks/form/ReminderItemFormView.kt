package com.habitrpg.android.habitica.ui.views.tasks.form

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.TimePicker
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.TaskFormReminderItemBinding
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.extensions.getThemeColor
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.models.tasks.RemindersItem
import com.habitrpg.android.habitica.models.tasks.Task
import java.text.DateFormat
import java.util.*

class ReminderItemFormView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    private val formattedTime: CharSequence?
        get() {
            val time = item.time
            return if (time != null) {
                formatter.format(time)
            } else {
                ""
            }
        }
    private val binding = TaskFormReminderItemBinding.inflate(context.layoutInflater, this)

    private val formatter: DateFormat
        get() {
            return if (taskType == Task.TYPE_DAILY) {
                DateFormat.getTimeInstance(DateFormat.SHORT)
            } else {
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
            }
        }

    var taskType = Task.TYPE_DAILY
    var item: RemindersItem = RemindersItem()
        set(value) {
            field = value
            binding.textView.text = formattedTime
        }

    var firstDayOfWeek: Int? = null

    var tintColor: Int = context.getThemeColor(R.attr.taskFormTint)
    var valueChangedListener: ((Date) -> Unit)? = null
    var animDuration = 0L
    var isAddButton: Boolean = true
        set(value) {
            field = value
            binding.textView.text = if (value) context.getString(R.string.new_reminder) else formattedTime
            if (value) {
                val rotate = RotateAnimation(135f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                rotate.duration = animDuration
                rotate.interpolator = LinearInterpolator()
                rotate.fillAfter = true
                binding.button.startAnimation(rotate)
                // This button is not clickable in this state, so make screen readers skip it.
                binding.button.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            } else {
                val rotate = RotateAnimation(0f, 135f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                rotate.duration = animDuration
                rotate.interpolator = LinearInterpolator()
                rotate.fillAfter = true
                binding.button.startAnimation(rotate)
                // This button IS now clickable, so allow screen readers to focus it.
                binding.button.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            }
        }

    init {
        minimumHeight = 38.dpToPx(context)
        background = ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_task_form)
        background.mutate().setTint(ContextCompat.getColor(context, R.color.taskform_gray))
        gravity = Gravity.CENTER_VERTICAL

        binding.button.setOnClickListener {
            if (!isAddButton) {
                (parent as? ViewGroup)?.removeView(this)
            }
        }
        // It's ok to make the description always be 'Delete Reminder' because when this button is
        // a plus button we set it as 'unimportant for accessibility' so it can't be focused.
        binding.button.contentDescription = context.getString(R.string.delete_reminder)
        binding.button.drawable.mutate().setTint(tintColor)

        binding.textView.setOnClickListener {
            val calendar = Calendar.getInstance()
            item.time?.let { calendar.time = it }
            if (taskType == Task.TYPE_DAILY) {
                val timePickerDialog = TimePickerDialog(
                    context, this,
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    android.text.format.DateFormat.is24HourFormat(context)
                )
                timePickerDialog.show()
            } else {
                val timePickerDialog = DatePickerDialog(
                    context, this,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                if ((firstDayOfWeek ?: -1) >= 0) {
                    timePickerDialog.datePicker.firstDayOfWeek = firstDayOfWeek ?: 0
                }
                timePickerDialog.show()
            }
        }
        binding.textView.labelFor = binding.button.id
    }
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        valueChangedListener?.let {
            val calendar = Calendar.getInstance()
            item.time?.let { calendar.time = it }
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            item.time = calendar.time
            binding.textView.text = formattedTime
            item.time?.let { date -> it(date) }
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        valueChangedListener?.let {
            val calendar = Calendar.getInstance()
            item.time?.let { calendar.time = it }
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            item.time = calendar.time
            binding.textView.text = formattedTime
            item.time?.let { date -> it(date) }

            val timePickerDialog = TimePickerDialog(
                context, this,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                android.text.format.DateFormat.is24HourFormat(context)
            )
            timePickerDialog.show()
        }
    }
}
