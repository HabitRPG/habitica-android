package com.habitrpg.android.habitica.helpers

import android.content.Context
import android.icu.text.MessageFormat
import android.os.Build
import android.text.format.DateUtils
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.common.habitica.extensionsCommon.nameSentenceRes
import com.habitrpg.shared.habitica.models.tasks.Frequency
import com.habitrpg.shared.habitica.models.tasks.TaskDifficulty
import com.habitrpg.shared.habitica.models.tasks.TaskType
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class TaskDescriptionBuilder(private val context: Context) {
    fun describe(task: Task): String {
        return when (task.type) {
            TaskType.HABIT ->
                context.getString(
                    R.string.habit_summary_description,
                    describeHabitDirections(task.up ?: false, task.down ?: false),
                    describeDifficulty(task.priority),
                )

            TaskType.TODO -> {
                if (task.dueDate != null) {
                    context.getString(
                        R.string.todo_summary_description_duedate,
                        describeDifficulty(task.priority),
                        describeDate(task.dueDate!!),
                    )
                } else {
                    context.getString(
                        R.string.todo_summary_description,
                        describeDifficulty(task.priority),
                    )
                }
            }

            TaskType.DAILY ->
                context.getString(
                    R.string.daily_summary_description,
                    describeDifficulty(task.priority),
                    describeRepeatInterval(task.frequency, task.everyX ?: 1),
                    describeRepeatDays(task),
                )

            else -> ""
        }
    }

    private val dateFormatter = DateFormat.getDateInstance()

    private fun describeDate(date: Date): String {
        return dateFormatter.format(date)
    }

    private fun describeRepeatDays(task: Task): Any {
        if (task.everyX == 0) {
            return ""
        }
        return when (task.frequency) {
            Frequency.WEEKLY -> {
                " " +
                    if (task.repeat?.isEveryDay == true) {
                        context.getString(R.string.on_every_day_of_week)
                    } else {
                        if (task.repeat?.isOnlyWeekdays == true) {
                            context.getString(R.string.on_weekdays)
                        } else if (task.repeat?.isOnlyWeekends == true) {
                            context.getString(R.string.on_weekends)
                        } else {
                            val dayStrings = task.repeat?.dayStrings(context) ?: listOf()
                            joinToCount(dayStrings)
                        }
                    }
            }

            Frequency.MONTHLY -> {
                " " +
                    if (task.getDaysOfMonth()?.isNotEmpty() == true) {
                        val dayList =
                            task.getDaysOfMonth()?.map {
                                withOrdinal(it)
                            }
                        context.getString(R.string.on_the_x, joinToCount(dayList))
                    } else if (task.getWeeksOfMonth()?.isNotEmpty() == true) {
                        val occurrence =
                            when (task.getWeeksOfMonth()?.first()) {
                                0 -> context.getString(R.string.first)
                                1 -> context.getString(R.string.second)
                                2 -> context.getString(R.string.third)
                                3 -> context.getString(R.string.fourth)
                                4 -> context.getString(R.string.fifth)
                                else -> return ""
                            }
                        val dayStrings = task.repeat?.dayStrings(context) ?: listOf()

                        context.getString(
                            R.string.on_the_x_of_month,
                            occurrence,
                            joinToCount(dayStrings),
                        )
                    } else {
                        ""
                    }
            }

            Frequency.YEARLY ->
                " " +
                    context.getString(
                        R.string.on_x,
                        task.startDate?.let {
                            val flags = DateUtils.FORMAT_SHOW_DATE + DateUtils.FORMAT_NO_YEAR
                            DateUtils.formatDateTime(context, it.time, flags)
                        } ?: "",
                    )

            else -> ""
        }
    }

    private fun joinToCount(dayStrings: List<String>?) =
        if (dayStrings?.size == 2) {
            context.getString(R.string.x_and_y, dayStrings[0], dayStrings[1])
        } else {
            dayStrings?.joinToString(", ") ?: ""
        }

    private fun withOrdinal(day: Int): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val formatter = MessageFormat("{0,ordinal}", Locale.getDefault())
            formatter.format(arrayOf(day))
        } else {
            day.toString()
        }
    }

    private fun describeRepeatInterval(
        interval: Frequency?,
        everyX: Int,
    ): String {
        if (everyX == 0) {
            return context.getString(R.string.never)
        }
        return when (interval) {
            Frequency.DAILY ->
                context.resources.getQuantityString(
                    R.plurals.repeat_daily,
                    everyX,
                    everyX,
                )

            Frequency.WEEKLY ->
                context.resources.getQuantityString(
                    R.plurals.repeat_weekly,
                    everyX,
                    everyX,
                )

            Frequency.MONTHLY ->
                context.resources.getQuantityString(
                    R.plurals.repeat_monthly,
                    everyX,
                    everyX,
                )

            Frequency.YEARLY ->
                context.resources.getQuantityString(
                    R.plurals.repeat_yearly,
                    everyX,
                    everyX,
                )

            null -> ""
        }
    }

    private fun describeHabitDirections(
        up: Boolean,
        down: Boolean,
    ): String {
        return if (up && down) {
            context.getString(R.string.positive_and_negative)
        } else if (up) {
            context.getString(R.string.positive_sentence)
        } else {
            context.getString(R.string.negative_sentence)
        }
    }

    private fun describeDifficulty(difficulty: Float): String {
        return context.getString(TaskDifficulty.valueOf(difficulty).nameSentenceRes)
    }
}
