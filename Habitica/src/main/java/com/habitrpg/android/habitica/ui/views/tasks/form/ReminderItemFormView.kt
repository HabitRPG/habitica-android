package com.habitrpg.android.habitica.ui.views.tasks.form

import android.app.TimePickerDialog
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TimePicker
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.tasks.RemindersItem
import com.habitrpg.android.habitica.ui.helpers.bindView
import java.text.DateFormat
import java.util.*


class ReminderItemFormView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), TimePickerDialog.OnTimeSetListener {


    private val button: ImageButton by bindView(R.id.button)
    private val textView: TextView by bindView(R.id.text_view)

    private val formatter = DateFormat.getTimeInstance(DateFormat.SHORT)

    var item: RemindersItem = RemindersItem()
    set(value) {
        field = value
        textView.text = formatter.format(item.time)
    }

    var tintColor: Int = ContextCompat.getColor(context, R.color.brand_300)
    var valueChangedListener: ((Date) -> Unit)? = null
    var animDuration = 0L
    var isAddButton: Boolean = true
    set(value) {
        field = value
        textView.text = if (value) context.getString(R.string.new_reminder) else formatter.format(item.time)
        if (value) {
            val rotate = RotateAnimation(135f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            rotate.duration = animDuration
            rotate.interpolator = LinearInterpolator()
            rotate.fillAfter = true
            button.startAnimation(rotate)
        } else {
            val rotate = RotateAnimation(0f, 135f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            rotate.duration = animDuration
            rotate.interpolator = LinearInterpolator()
            rotate.fillAfter = true
            button.startAnimation(rotate)
        }
    }

    init {
        minimumHeight = 38.dpToPx(context)
        inflate(R.layout.task_form_reminder_item, true)
        background = context.getDrawable(R.drawable.layout_rounded_bg_task_form)
        background.mutate().setTint(ContextCompat.getColor(context, R.color.taskform_gray))
        gravity = Gravity.CENTER_VERTICAL

        button.setOnClickListener {
            if (!isAddButton) {
                (parent as? ViewGroup)?.removeView(this)
            }
        }
        button.drawable.mutate().setTint(tintColor)

        textView.setOnClickListener {
            val calendar = Calendar.getInstance()
            item.time?.let { calendar.time = it }
            val timePickerDialog = TimePickerDialog(context, this,
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    android.text.format.DateFormat.is24HourFormat(context))
            timePickerDialog.show()
        }
    }
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        valueChangedListener?.let {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            item.time = calendar.time
            textView.text = formatter.format(item.time)
            item.time?.let { date -> it(date) }
        }
    }
}