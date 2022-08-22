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
import com.habitrpg.android.habitica.models.tasks.RemindersItem
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.shared.habitica.models.tasks.TaskType
import java.text.DateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class ReminderItemFormView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    private val formattedTime: CharSequence?
        get() {
            return if (item.time != null) {
                val time = Date.from(item.getLocalZonedDateTimeInstant())
                formatter.format(time)
            } else {
                ""
            }
        }

    private val binding = TaskFormReminderItemBinding.inflate(context.layoutInflater, this)

    private val formatter: DateFormat
        get() {
            return if (taskType == TaskType.DAILY) {
                DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())
            } else {
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
            }
        }

    var taskType = TaskType.DAILY
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
            if (taskType == TaskType.DAILY) {
                val timePickerDialog = TimePickerDialog(
                    context, this,
                    item.getZonedDateTime()?.hour ?: ZonedDateTime.now().hour,
                    item.getZonedDateTime()?.minute ?: ZonedDateTime.now().minute,
                    android.text.format.DateFormat.is24HourFormat(context)
                )
                timePickerDialog.show()
            } else {
                val zonedDateTime = (item.getZonedDateTime() ?: ZonedDateTime.now())
                val timePickerDialog = DatePickerDialog(
                    context, this,
                    zonedDateTime.year,
                    zonedDateTime.monthValue - 1,
                    zonedDateTime.dayOfMonth
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
            val zonedDateTime = (item.getZonedDateTime() ?: ZonedDateTime.now())
                .withHour(hourOfDay)
                .withMinute(minute)
            item.time = zonedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            binding.textView.text = formattedTime
            it(Date.from(item.getLocalZonedDateTimeInstant()))
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        valueChangedListener?.let {
            val zonedDateTime = ZonedDateTime.now()
                .withYear(year)
                .withMonth(month + 1)
                .withDayOfMonth(dayOfMonth)
            item.time = zonedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            binding.textView.text = formattedTime
            it(Date.from(item.getLocalZonedDateTimeInstant()))

            val timePickerDialog = TimePickerDialog(
                context, this,
                ZonedDateTime.now().hour,
                ZonedDateTime.now().minute,
                android.text.format.DateFormat.is24HourFormat(context)
            )
            timePickerDialog.show()
        }
    }
}
