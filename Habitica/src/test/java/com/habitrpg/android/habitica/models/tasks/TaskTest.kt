package com.habitrpg.android.habitica.models.tasks

import com.habitrpg.android.habitica.extensions.matchesRepeatDays
import com.habitrpg.android.habitica.extensions.toZonedDateTime
import com.habitrpg.shared.habitica.models.tasks.Frequency
import com.habitrpg.shared.habitica.models.tasks.TaskType
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import java.time.format.DateTimeFormatter
import java.util.Calendar

class TaskTest : WordSpec({
    "getNextReminderOccurrences" When {
        var daily = Task()
        var reminder = RemindersItem()
        var calendar = Calendar.getInstance()
        beforeEach {
            daily = Task()
            daily.type = TaskType.DAILY

            reminder = RemindersItem()

            calendar = Calendar.getInstance()
        }
        "dailies repeating daily" should {
            beforeEach {
                daily.frequency = Frequency.DAILY
            }
            "return occurrences according to everyX = 2" {
                calendar.add(Calendar.DATE, -2)
                daily.startDate = calendar.time
                daily.everyX = 2
                calendar.add(Calendar.DATE, 4)
                reminder.time = calendar.time.toZonedDateTime()?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                val occurrences = daily.getNextReminderOccurrences(reminder, 4)
                occurrences?.size shouldBe 4
                occurrences?.forEach {
                    it.dayOfYear shouldBe calendar.get(Calendar.DAY_OF_YEAR)
                    calendar.add(Calendar.DATE, 2)
                }
            }

            "return occurrences according to everyX = 1" {
                calendar.add(Calendar.DATE, -2)
                daily.startDate = calendar.time
                daily.everyX = 1
                calendar.add(Calendar.DATE, 3)
                reminder.time = calendar.time.toZonedDateTime()?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                val occurrences = daily.getNextReminderOccurrences(reminder, 4)
                occurrences?.size shouldBe 4
                occurrences?.forEach {
                    it.dayOfYear shouldBe calendar.get(Calendar.DAY_OF_YEAR)
                    calendar.add(Calendar.DATE, 1)
                }
            }

            "return occurrences according to everyX = 5" {
                calendar.add(Calendar.DATE, -33)
                daily.startDate = calendar.time
                daily.everyX = 5
                reminder.time = calendar.time.toZonedDateTime()?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                val occurrences = daily.getNextReminderOccurrences(reminder, 4)
                occurrences?.size shouldBe 4
                calendar.add(Calendar.DATE, 35)
                occurrences?.forEach {
                    it.dayOfYear shouldBe calendar.get(Calendar.DAY_OF_YEAR)
                    calendar.add(Calendar.DATE, 5)
                }
            }
        }
        "dailies repeating weekly" should {
            beforeEach {
                daily.frequency = Frequency.WEEKLY
            }
            "return occurrences if active every day" {
                daily.startDate = calendar.time
                daily.repeat = Days()
                daily.everyX = 1

                reminder.time = calendar.time.toZonedDateTime()?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                val occurrences = daily.getNextReminderOccurrences(reminder, 4)
                occurrences?.size shouldBe 4
                calendar.add(Calendar.DATE, 1)
                occurrences?.forEach {
                    it.dayOfYear shouldBe calendar.get(Calendar.DAY_OF_YEAR)
                    calendar.add(Calendar.DATE, 1)
                }
            }

            "return occurrences if active tuesdays" {
                calendar.add(Calendar.DATE, -63)
                daily.startDate = calendar.time
                daily.repeat = Days(t = true, default = false)
                daily.everyX = 1

                reminder.time = calendar.time.toZonedDateTime()?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                val occurrences = daily.getNextReminderOccurrences(reminder, 4)
                occurrences?.size shouldBe 4
                calendar.add(Calendar.DATE, 63)
                while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.TUESDAY) {
                    calendar.add(Calendar.DATE, 1)
                }
                occurrences?.forEach {
                    it.dayOfYear shouldBe calendar.get(Calendar.DAY_OF_YEAR)
                    calendar.add(Calendar.DATE, 7)
                }
            }

            "return occurrences if active every second friday" {
                calendar.add(Calendar.DATE, -23)
                daily.startDate = calendar.time
                daily.repeat = Days(f = true, default = false)
                daily.everyX = 2

                reminder.time = calendar.time.toZonedDateTime()?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                val occurrences = daily.getNextReminderOccurrences(reminder, 4)
                occurrences?.size shouldBe 4
                while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
                    calendar.add(Calendar.DATE, 1)
                }
                calendar.add(Calendar.DATE, 28)
                occurrences?.forEach {
                    it.dayOfYear shouldBe calendar.get(Calendar.DAY_OF_YEAR)
                    calendar.add(Calendar.DATE, 14)
                }
            }

            "return occurrences if active multiple days a week" {
                calendar.add(Calendar.DATE, -23)
                daily.startDate = calendar.time
                daily.repeat = Days(t = true, f = true, s = true, default = false)
                daily.everyX = 1

                reminder.time = calendar.time.toZonedDateTime()?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                val occurrences = daily.getNextReminderOccurrences(reminder, 4)
                occurrences?.size shouldBe 4
                calendar.add(Calendar.DATE, 23)
                occurrences?.forEach {
                    while (!calendar.matchesRepeatDays(daily.repeat)) {
                        calendar.add(Calendar.DATE, 1)
                    }
                    it.dayOfYear shouldBe calendar.get(Calendar.DAY_OF_YEAR)
                    calendar.add(Calendar.DATE, 1)
                }
            }
        }

        "dailies repeating monthly" should {
            beforeEach {
                daily.frequency = Frequency.MONTHLY
            }

            "return occurrences if active every 10th of the month" {
                daily.startDate = calendar.time
                daily.everyX = 1
                daily.setDaysOfMonth(listOf(10))

                reminder.time = calendar.time.toZonedDateTime()?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                val occurrences = daily.getNextReminderOccurrences(reminder, 4)
                occurrences?.size shouldBe 4
                occurrences?.forEach {
                    while (calendar.get(Calendar.DAY_OF_MONTH) != 10) {
                        calendar.add(Calendar.DATE, 1)
                    }
                    it.dayOfYear shouldBe calendar.get(Calendar.DAY_OF_YEAR)
                    it.year shouldBe calendar.get(Calendar.YEAR)
                    calendar.add(Calendar.DATE, 1)
                }
            }

            "return occurrences if active every third month on the 5th" {
                calendar.add(Calendar.MONDAY, -8)
                daily.startDate = calendar.time
                daily.everyX = 3
                daily.setDaysOfMonth(listOf(5))

                reminder.time = calendar.time.toZonedDateTime()?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                val occurrences = daily.getNextReminderOccurrences(reminder, 4)
                calendar.add(Calendar.MONTH, 9)
                occurrences?.size shouldBe 4
                occurrences?.forEach {
                    while (calendar.get(Calendar.DAY_OF_MONTH) != 5) {
                        calendar.add(Calendar.DATE, 1)
                    }
                    it.dayOfYear shouldBe calendar.get(Calendar.DAY_OF_YEAR)
                    it.year shouldBe calendar.get(Calendar.YEAR)
                    calendar.add(Calendar.MONTH, 3)
                }
            }

            "return occurrences if active every month on the third tuesday" {
                calendar.set(Calendar.YEAR, 2025)
                calendar.set(Calendar.MONTH, Calendar.JANUARY)
                calendar.set(Calendar.DAY_OF_MONTH, 21)
                daily.startDate = calendar.time
                daily.everyX = 1
                daily.setWeeksOfMonth(listOf(2))

                reminder.time = calendar.time.toZonedDateTime()?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                val occurrences = daily.getNextReminderOccurrences(reminder, 4)
                occurrences?.size shouldBe 4
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                occurrences?.forEach {
                    while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.TUESDAY) {
                        calendar.add(Calendar.DATE, 1)
                    }
                    calendar.add(Calendar.DATE, 14)
                    it.dayOfYear shouldBe calendar.get(Calendar.DAY_OF_YEAR)
                    it.year shouldBe calendar.get(Calendar.YEAR)
                    calendar.add(Calendar.MONTH, 1)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                }
            }

            "return occurrences if active every fifth month on the second wednesday" {
                calendar.add(Calendar.MONTH, -8)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY) {
                    calendar.add(Calendar.DATE, 1)
                }
                calendar.add(Calendar.DATE, 7)
                daily.startDate = calendar.time
                daily.everyX = 5
                daily.setWeeksOfMonth(listOf(1))

                reminder.time = calendar.time.toZonedDateTime()?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                val occurrences = daily.getNextReminderOccurrences(reminder, 4)
                occurrences?.size shouldBe 4
                calendar.add(Calendar.MONTH, 10)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                occurrences?.forEach {
                    while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY) {
                        calendar.add(Calendar.DATE, 1)
                    }
                    calendar.add(Calendar.DATE, 7)
                    print(daily.startDate)
                    print(it)
                    println(calendar.time)
                    it.dayOfYear shouldBe calendar.get(Calendar.DAY_OF_YEAR)
                    it.year shouldBe calendar.get(Calendar.YEAR)
                    calendar.add(Calendar.MONTH, 5)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                }
            }
        }

        "dailies repeating yearly" should {
            beforeEach {
                daily.frequency = Frequency.YEARLY
            }
            "return occurrences if active every year" {
                daily.startDate = calendar.time
                daily.everyX = 1
                reminder.time = calendar.time.toZonedDateTime()?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                val occurrences = daily.getNextReminderOccurrences(reminder, 4)
                occurrences?.size shouldBe 4
                calendar.add(Calendar.YEAR, 1)
                occurrences?.forEach {
                    it.dayOfYear shouldBe calendar.get(Calendar.DAY_OF_YEAR)
                    it.year shouldBe calendar.get(Calendar.YEAR)
                    calendar.add(Calendar.YEAR, 1)
                }
            }

            "return occurrences if active every 3 years" {
                calendar.add(Calendar.YEAR, -3)
                calendar.add(Calendar.DATE, 1)
                daily.startDate = calendar.time
                daily.everyX = 3
                reminder.time = calendar.time.toZonedDateTime()?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                calendar.add(Calendar.YEAR, 3)
                val occurrences = daily.getNextReminderOccurrences(reminder, 4)
                occurrences?.size shouldBe 4
                occurrences?.forEach {
                    it.dayOfYear shouldBe calendar.get(Calendar.DAY_OF_YEAR)
                    it.year shouldBe calendar.get(Calendar.YEAR)
                    calendar.add(Calendar.YEAR, 3)
                }
            }
        }
    }
})
