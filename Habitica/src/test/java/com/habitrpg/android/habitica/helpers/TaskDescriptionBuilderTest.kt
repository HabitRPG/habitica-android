package com.habitrpg.android.habitica.helpers

import android.content.Context
import android.content.res.Resources
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.tasks.Days
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.shared.habitica.models.tasks.Frequency
import com.habitrpg.shared.habitica.models.tasks.TaskType
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.mockk.every
import io.mockk.mockk
import java.util.Date

/**
 * Resolves resources to their names instead of the translated text, so a description reads like
 * `daily_summary_description(easy_sentence|repeat_weekly[1]| on_weekdays|)`.
 */
private fun fakeContext(): Context {
    val stringNames = R.string::class.java.fields.associate { it.getInt(null) to it.name }
    val pluralNames = R.plurals::class.java.fields.associate { it.getInt(null) to it.name }
    val resources = mockk<Resources>()
    every { resources.getQuantityString(any(), any(), *anyVararg()) } answers {
        "${pluralNames[firstArg()]}[${secondArg<Int>()}]"
    }
    val context = mockk<Context>()
    every { context.resources } returns resources
    every { context.getString(any()) } answers { stringNames[firstArg()] ?: "unknown" }
    every { context.getString(any(), *anyVararg()) } answers {
        val formatArgs = args.drop(1).flatMap { if (it is Array<*>) it.toList() else listOf(it) }.joinToString("|")
        "${stringNames[firstArg()]}($formatArgs)"
    }
    return context
}

class TaskDescriptionBuilderTest :
    WordSpec({
        val builder = TaskDescriptionBuilder(fakeContext())

        fun days(vararg active: String) =
            Days().apply {
                m = "m" in active
                t = "t" in active
                w = "w" in active
                th = "th" in active
                f = "f" in active
                s = "s" in active
                su = "su" in active
            }

        "describe habits" should {
            fun habit(
                up: Boolean,
                down: Boolean,
            ) = Task().apply {
                type = TaskType.HABIT
                priority = 1f
                this.up = up
                this.down = down
            }

            "describe positive and negative habits" {
                builder.describe(habit(up = true, down = true)) shouldBe
                    "habit_summary_description(positive_and_negative|easy_sentence)"
            }

            "describe positive habits" {
                builder.describe(habit(up = true, down = false)) shouldBe
                    "habit_summary_description(positive_sentence|easy_sentence)"
            }

            "describe negative habits" {
                builder.describe(habit(up = false, down = true)) shouldBe
                    "habit_summary_description(negative_sentence|easy_sentence)"
            }
        }

        "describe difficulty" should {
            "map every priority to its sentence" {
                mapOf(0.1f to "trivial_sentence", 1f to "easy_sentence", 1.5f to "medium_sentence", 2f to "hard_sentence")
                    .forEach { (priority, sentence) ->
                        builder.describe(
                            Task().apply {
                                type = TaskType.TODO
                                this.priority = priority
                            },
                        ) shouldBe "todo_summary_description($sentence)"
                    }
            }
        }

        "describe todos" should {
            "mention the due date" {
                val description =
                    builder.describe(
                        Task().apply {
                            type = TaskType.TODO
                            priority = 2f
                            dueDate = Date()
                        },
                    )
                description shouldStartWith "todo_summary_description_duedate(hard_sentence|"
            }
        }

        "describe dailies" should {
            fun daily(
                frequency: Frequency,
                everyX: Int = 1,
                repeat: Days? = null,
            ) = Task().apply {
                type = TaskType.DAILY
                priority = 1f
                this.frequency = frequency
                this.everyX = everyX
                this.repeat = repeat
            }

            "describe daily repeats" {
                builder.describe(daily(Frequency.DAILY, everyX = 3)) shouldBe
                    "daily_summary_description(easy_sentence|repeat_daily[3]||)"
            }

            "describe dailies that never repeat" {
                builder.describe(daily(Frequency.DAILY, everyX = 0)) shouldBe
                    "daily_summary_description(easy_sentence|never||)"
            }

            "describe weekly dailies on every day" {
                builder.describe(daily(Frequency.WEEKLY, repeat = Days())) shouldBe
                    "daily_summary_description(easy_sentence|repeat_weekly[1]| on_every_day_of_week|)"
            }

            "describe weekly dailies on weekdays" {
                builder.describe(daily(Frequency.WEEKLY, repeat = days("m", "t", "w", "th", "f"))) shouldBe
                    "daily_summary_description(easy_sentence|repeat_weekly[1]| on_weekdays|)"
            }

            "describe weekly dailies on weekends" {
                builder.describe(daily(Frequency.WEEKLY, everyX = 2, repeat = days("s", "su"))) shouldBe
                    "daily_summary_description(easy_sentence|repeat_weekly[2]| on_weekends|)"
            }

            "join two days with and" {
                builder.describe(daily(Frequency.WEEKLY, repeat = days("m", "th"))) shouldBe
                    "daily_summary_description(easy_sentence|repeat_weekly[1]| x_and_y(monday|thursday)|)"
            }

            "list more than two days" {
                builder.describe(daily(Frequency.WEEKLY, repeat = days("m", "w", "f"))) shouldBe
                    "daily_summary_description(easy_sentence|repeat_weekly[1]| monday, wednesday, friday|)"
            }

            "describe yearly dailies without a start date" {
                builder.describe(daily(Frequency.YEARLY)) shouldBe
                    "daily_summary_description(easy_sentence|repeat_yearly[1]| on_x()|)"
            }
        }

        "describe rewards" should {
            "be empty" {
                builder.describe(Task().apply { type = TaskType.REWARD }) shouldBe ""
            }
        }
    })
