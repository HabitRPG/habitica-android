package com.habitrpg.android.habitica.helpers

import android.content.Context
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.shared.habitica.models.tasks.TaskType
import java.text.DateFormat
import java.util.Date

class TaskDescriptionBuilder(private val context: Context) {

    fun describe(task: Task): String {
        return when (task.type) {
            TaskType.HABIT -> context.getString(R.string.habit_summary_description, describeHabitDirections(task.up ?: false, task.down ?: false), describeDifficulty(task.priority))
            TaskType.TODO -> {
                if (task.dueDate != null) {
                    context.getString(R.string.todo_summary_description_duedate, describeDifficulty(task.priority), describeDate(task.dueDate!!))
                } else {
                    context.getString(R.string.todo_summary_description, describeDifficulty(task.priority))
                }
            }
            TaskType.DAILY -> context.getString(R.string.daily_summary_description, describeDifficulty(task.priority), "sometimes")
            else -> ""
        }
    }

    private val dateFormatter = DateFormat.getDateInstance()

    private fun describeDate(date: Date): String {
        return dateFormatter.format(date)
    }

    private fun describeHabitDirections(up: Boolean, down: Boolean): String {
        return if (up && down) {
            context.getString(R.string.positive_and_negative)
        } else if (up) {
            context.getString(R.string.positive_habit_form)
        } else {
            context.getString(R.string.negative_habit_form)
        }
    }

    private fun describeDifficulty(difficulty: Float): String {
        return when (difficulty) {
            0.1f -> context.getString(R.string.trivial)
            1.0f -> context.getString(R.string.easy)
            1.5f -> context.getString(R.string.medium)
            2.0f -> context.getString(R.string.hard)
            else -> ""
        }
    }
}
