package com.habitrpg.android.habitica.models

import com.habitrpg.android.habitica.models.tasks.Task
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import java.util.Date
import java.time.ZonedDateTime
import java.util.Calendar

class TaskTest : WordSpec({
    val task = Task()

    fun date(
            year: Int = 1989,
            month: Int = 1,
            day: Int = 2,
            hourOfDay: Int = 3,
            minute: Int = 4,
            second: Int = 5
    ) : Date {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, hourOfDay, minute, second)

        return calendar.time
    }

    "isDueToday" should {
        "false if the day of year is before today" {
            val now =  ZonedDateTime.now()
            task.dueDate = date(day= now.dayOfYear - 1, year = now.year)

            task.isDueToday() shouldBe false
        }

        "false if the year is before today" {
            val now =  ZonedDateTime.now()
            task.dueDate = date(day= now.dayOfYear, year = now.year - 1)

            task.isDueToday() shouldBe false
        }

        "true if the day of year is after today" {
            val now =  ZonedDateTime.now()
            task.dueDate = date(day= now.dayOfYear + 1, year = now.year)

            task.isDueToday() shouldBe true
        }

        "true if the year is after today" {
            val now =  ZonedDateTime.now()
            task.dueDate = date(day= now.dayOfYear, year = now.year + 1)

            task.isDueToday() shouldBe true
        }

        "true if it is today" {
            val now =  ZonedDateTime.now()
            task.dueDate = date(day= now.dayOfYear, year = now.year)

            task.isDueToday() shouldBe true
        }
    }
})