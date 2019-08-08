package com.habitrpg.android.habitica.ui.views.tasks.form

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.icu.text.MessageFormat
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.tasks.Days
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.adapter.SimpleSpinnerAdapter
import com.habitrpg.android.habitica.ui.helpers.bindView
import java.text.DateFormat
import java.text.DateFormatSymbols
import java.util.*

class TaskSchedulingControls @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), DatePickerDialog.OnDateSetListener {

    var tintColor: Int = ContextCompat.getColor(context, R.color.brand_300)

    private val startDateWrapper: ViewGroup by bindView(R.id.start_date_wrapper)
    private val startDateTitleView: TextView by bindView(R.id.start_date_title)
    private val startDateTextView: TextView by bindView(R.id.start_date_textview)
    private val repeatsEveryWrapper: ViewGroup by bindView(R.id.repeats_every_wrapper)
    private val repeatsEverySpinner: Spinner by bindView(R.id.repeats_every_spinner)
    private val repeatsEveryEdittext: AppCompatEditText by bindView(R.id.repeats_every_edittext)
    private val repeatsEveryTitleView: TextView by bindView(R.id.repeats_every_title)
    private val weeklyRepeatWrapper: ViewGroup by bindView(R.id.weekly_repeat_wrapper)
    private val monthlyRepeatWrapper: ViewGroup by bindView(R.id.monthly_repeat_wrapper)
    private val monthlyRepeatDaysButton: TextView by bindView(R.id.monthly_repeat_days)
    private val monthlyRepeatWeeksButton: TextView by bindView(R.id.monthly_repeat_weeks)
    private val summaryTextView: TextView by bindView(R.id.summary_textview)

    private val dateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM)
    private val frequencyAdapter = SimpleSpinnerAdapter(context, R.array.repeatables_frequencies)

    var taskType = Task.TYPE_DAILY
    set(value) {
        field = value
        configureViewsForType()
    }
    var startDate = Date()
    set(value) {
        field = value
        startDateTextView.text = dateFormatter.format(value)
        startDateCalendar.time = value
        generateSummary()
    }
    private var startDateCalendar = Calendar.getInstance()
    var dueDate: Date? = null
    set(value) {
        field = value
        value?.let { startDateTextView.text = dateFormatter.format(it) }
    }
    var frequency = Task.FREQUENCY_DAILY
    set(value) {
        field = value
        repeatsEverySpinner.setSelection(when (value) {
            Task.FREQUENCY_WEEKLY -> 1
            Task.FREQUENCY_MONTHLY -> 2
            Task.FREQUENCY_YEARLY -> 3
            else -> 0
        })
        configureViewsForFrequency()
        generateSummary()
    }
    var everyX
        get() = (repeatsEveryEdittext.text ?: "1").toString().toIntOrNull() ?: 1
    set(value) {
        try {
            repeatsEveryEdittext.setText(value.toString())
        } catch (e: NumberFormatException) {
            repeatsEveryEdittext.setText("1")
        }
        generateSummary()
    }
    var weeklyRepeat: Days = Days()
    set(value) {
        field = value
        createWeeklyRepeatViews()
        generateSummary()
    }

    var daysOfMonth: List<Int>? = null
    set(value) {
        field = value
        configureMonthlyRepeatViews()
        generateSummary()
    }
    var weeksOfMonth: List<Int>? = null
    set(value) {
        field = value
        configureMonthlyRepeatViews()
        generateSummary()
    }

    private val weekdays: Array<String> by lazy {
        DateFormatSymbols().weekdays
    }
    private val weekdayOrder: List<Int> by lazy {
        val codes = (1..7).toList()
        Collections.rotate(codes, -startDateCalendar.firstDayOfWeek)
        codes
    }

    init {
        inflate(R.layout.task_form_task_scheduling, true)
        repeatsEverySpinner.adapter = frequencyAdapter

        frequency = Task.FREQUENCY_WEEKLY
        startDate = Date()
        everyX = 1
        weeklyRepeat = Days()

        repeatsEverySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                frequency = frequency
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                frequency = when (position) {
                    1 -> Task.FREQUENCY_WEEKLY
                    2 -> Task.FREQUENCY_MONTHLY
                    3 -> Task.FREQUENCY_YEARLY
                    else -> Task.FREQUENCY_DAILY
                }
            }
        }

        startDateWrapper.setOnClickListener {
            val datePickerDialog = DatePickerDialog(context, this,
                    startDateCalendar.get(Calendar.YEAR),
                    startDateCalendar.get(Calendar.MONTH),
                    startDateCalendar.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, resources.getString(R.string.today)) { _, _ ->
                if (taskType == Task.TYPE_TODO) {
                    dueDate = Date()
                } else {
                    startDate = Date()
                }
            }
            if (taskType == Task.TYPE_TODO) {
                datePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, resources.getString(R.string.clear)) { _, _ ->
                    dueDate = null
                }
            }
            datePickerDialog.show()
        }

        monthlyRepeatDaysButton.setOnClickListener {
            daysOfMonth = mutableListOf(startDateCalendar.get(Calendar.DATE))
            weeksOfMonth = null
            generateSummary()
        }
        monthlyRepeatWeeksButton.setOnClickListener {
            weeksOfMonth = mutableListOf(startDateCalendar.get(Calendar.WEEK_OF_MONTH))
            daysOfMonth = null
            generateSummary()
        }

        orientation = VERTICAL
        configureViewsForType()
        configureViewsForFrequency()
    }

    private fun configureViewsForType() {
        startDateTitleView.text = context.getString(if (taskType == Task.TYPE_DAILY) R.string.start_date else R.string.due_date)
        repeatsEveryWrapper.visibility = if (taskType == Task.TYPE_DAILY) View.VISIBLE else View.GONE
        summaryTextView.visibility =  if (taskType == Task.TYPE_DAILY) View.VISIBLE else View.GONE
        weeklyRepeatWrapper.visibility = if (taskType == Task.TYPE_DAILY) View.VISIBLE else View.GONE
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        startDateCalendar.set(year, month, dayOfMonth)
        if (taskType == Task.TYPE_TODO) {
            dueDate = startDateCalendar.time
        } else {
            startDate = startDateCalendar.time
        }
    }

    private fun configureViewsForFrequency() {
        repeatsEveryTitleView.text = context.getText(when (frequency) {
            Task.FREQUENCY_WEEKLY -> R.string.weeks
            Task.FREQUENCY_MONTHLY -> R.string.months
            Task.FREQUENCY_YEARLY -> R.string.years
            else -> R.string.days
        })
        weeklyRepeatWrapper.visibility = if (frequency == Task.FREQUENCY_WEEKLY && taskType == Task.TYPE_DAILY) View.VISIBLE else View.GONE
        monthlyRepeatWrapper.visibility = if (frequency == Task.FREQUENCY_MONTHLY && taskType == Task.TYPE_DAILY) View.VISIBLE else View.GONE
        if (frequency == Task.FREQUENCY_WEEKLY) {
            createWeeklyRepeatViews()
        } else if (frequency == Task.FREQUENCY_MONTHLY) {
            if (weeksOfMonth?.isNotEmpty() != true && daysOfMonth?.isNotEmpty() != true) {
                daysOfMonth = listOf(startDateCalendar.get(Calendar.DATE))
            }
        }
    }

    private fun setWeekdayActive(weekday: Int, isActive: Boolean) {
        when (weekday) {
            2 -> weeklyRepeat.m = isActive
            3 -> weeklyRepeat.t = isActive
            4 -> weeklyRepeat.w = isActive
            5 -> weeklyRepeat.th = isActive
            6 -> weeklyRepeat.f = isActive
            7 -> weeklyRepeat.s = isActive
            1 -> weeklyRepeat.su = isActive
        }
        createWeeklyRepeatViews()
    }

    private fun isWeekdayActive(weekday: Int): Boolean {
        return when (weekday) {
            2 -> weeklyRepeat.m
            3 -> weeklyRepeat.t
            4 -> weeklyRepeat.w
            5 -> weeklyRepeat.th
            6 -> weeklyRepeat.f
            7 -> weeklyRepeat.s
            1 -> weeklyRepeat.su
            else -> false
        }
    }

    private fun createWeeklyRepeatViews() {
        weeklyRepeatWrapper.removeAllViews()
        val size = 32.dpToPx(context)
        val lastWeekday = weekdayOrder.last()
        for (weekdayCode in weekdayOrder) {
            val button = TextView(context, null, 0, R.style.TaskFormWeekdayButton)
            val layoutParams = LayoutParams(size, size)
            button.layoutParams = layoutParams
            button.text = weekdays[weekdayCode].first().toUpperCase().toString()
            val isActive = isWeekdayActive(weekdayCode)
            if (isActive) {
                button.background = context.getDrawable(R.drawable.habit_scoring_circle_selected)
                button.background.mutate().setTint(tintColor)
                button.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                button.background = context.getDrawable(R.drawable.habit_scoring_circle)
                button.setTextColor(ContextCompat.getColor(context, R.color.gray_100))
            }
            button.setOnClickListener {
                setWeekdayActive(weekdayCode, !isActive)
            }
            weeklyRepeatWrapper.addView(button)
            if (weekdayCode != lastWeekday) {
                val space = Space(context)
                val spaceLayoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT)
                spaceLayoutParams.weight = 1f
                space.layoutParams = spaceLayoutParams
                weeklyRepeatWrapper.addView(space)
            }
        }
    }

    private fun configureMonthlyRepeatViews() {
        val white = ContextCompat.getColor(context, R.color.white)
        val unselectedText = ContextCompat.getColor(context, R.color.gray_100)
        val unselectedBackground = ContextCompat.getColor(context, R.color.taskform_gray)
        if (daysOfMonth != null && daysOfMonth?.isEmpty() != true) {
            monthlyRepeatDaysButton.setTextColor(white)
            monthlyRepeatDaysButton.background.mutate().setTint(tintColor)
        } else {
            monthlyRepeatDaysButton.setTextColor(unselectedText)
            monthlyRepeatDaysButton.background.mutate().setTint(unselectedBackground)
        }
        if (weeksOfMonth != null && weeksOfMonth?.isEmpty() != true) {
            monthlyRepeatWeeksButton.setTextColor(white)
            monthlyRepeatWeeksButton.background.mutate().setTint(tintColor)
        } else {
            monthlyRepeatWeeksButton.setTextColor(unselectedText)
            monthlyRepeatWeeksButton.background.mutate().setTint(unselectedBackground)
        }
    }

    private fun generateSummary() {
        var frequencyQualifier = ""

        when (frequency) {
            "daily" ->  frequencyQualifier = if (everyX == 1) "day" else "days"
            "weekly" -> frequencyQualifier = if (everyX == 1) "week" else "weeks"
            "monthly" -> frequencyQualifier = if (everyX == 1) "month" else "months"
            "yearly" -> frequencyQualifier = if (everyX == 1) "year" else "years"
        }

        var weekdays = if (frequency == "weekly") {
            val weekdayStrings = ArrayList<String>()
            if (weeklyRepeat.m) {
                weekdayStrings.add("Monday")
            }
            if (weeklyRepeat.t) {
                weekdayStrings.add("Tuesday")
            }
            if (weeklyRepeat.w) {
                weekdayStrings.add("Wednesday")
            }
            if (weeklyRepeat.th) {
                weekdayStrings.add("Thursday")
            }
            if (weeklyRepeat.f) {
                weekdayStrings.add("Friday")
            }
            if (weeklyRepeat.s) {
                weekdayStrings.add("Saturday")
            }
            if (weeklyRepeat.su) {
                weekdayStrings.add("Sunday")
            }
            " on " + TextUtils.join(", ", weekdayStrings)
        } else {
            ""
        }

        if (frequency == "monthly") {
            weekdays = if (daysOfMonth?.isNotEmpty() == true) {
                val date = startDateCalendar.get(Calendar.DATE)
                val formattedDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val formatter = MessageFormat("{0,ordinal}", Locale.getDefault())
                    formatter.format(arrayOf(date))
                } else date.toString()
                " on the $formattedDate"
            } else {
                val week = startDateCalendar.get(Calendar.WEEK_OF_MONTH)
                val formattedWeek = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val formatter = MessageFormat("{0,ordinal}", Locale.getDefault())
                    formatter.format(arrayOf(week))
                } else week.toString()
                val dayLongName = startDateCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
                " on the $formattedWeek week on $dayLongName"
            }
        }

        val everyXString = if (everyX == 1) "" else "$everyX "

        val summary = resources.getString(R.string.repeat_summary, frequency, everyXString, frequencyQualifier, weekdays)
        summaryTextView.text = summary
    }
}