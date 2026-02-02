package com.habitrpg.android.habitica.models.tasks

import com.habitrpg.android.habitica.extensions.matchesRepeatDays
import com.habitrpg.android.habitica.extensions.toZonedDateTime
import com.habitrpg.shared.habitica.models.tasks.Frequency
import com.habitrpg.shared.habitica.models.tasks.TaskType
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

class TaskTest : WordSpec({
    listOf(
        Date(),
        Calendar.getInstance().apply {
            set(2025, 1, 1)
        }.time,
        Calendar.getInstance().apply {
            set(2024, Calendar.DECEMBER, 23)
        }.time
    ).forEach { day ->
        "getNextReminderOccurrences on $day" When {
            var daily = Task()
            var reminder = RemindersItem()
            var calendar = Calendar.getInstance()
            calendar.time = day
            val fakeZDT = day.toZonedDateTime()
            beforeEach {
                daily = Task()
                daily.type = TaskType.DAILY

                reminder = RemindersItem()

                calendar = Calendar.getInstance()
                calendar.time = day
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
                    reminder.time = fakeZDT?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                    val occurrences = daily.getNextReminderOccurrences(reminder, 4, fakeZDT)
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
                    reminder.time = fakeZDT?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                    val occurrences = daily.getNextReminderOccurrences(reminder, 4, fakeZDT)
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
                    reminder.time = fakeZDT?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                    val occurrences = daily.getNextReminderOccurrences(reminder, 4, fakeZDT)
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

                    reminder.time = fakeZDT?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                    val occurrences = daily.getNextReminderOccurrences(reminder, 4, fakeZDT)
                    occurrences?.size shouldBe 4
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

                    reminder.time = fakeZDT?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                    val occurrences = daily.getNextReminderOccurrences(reminder, 4, fakeZDT)
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
                    daily.startDate = calendar.time
                    daily.repeat = Days(f = true, default = false)
                    daily.everyX = 2

                    reminder.time = fakeZDT?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                    val occurrences = daily.getNextReminderOccurrences(reminder, 4, fakeZDT)
                    occurrences?.size shouldBe 4
                    while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
                        calendar.add(Calendar.DATE, 1)
                    }
                    occurrences?.forEach {
                        it.dayOfMonth shouldBe calendar.get(Calendar.DAY_OF_MONTH)
                        it.dayOfYear shouldBe calendar.get(Calendar.DAY_OF_YEAR)
                        calendar.add(Calendar.DATE, 14)
                    }
                }

                "return occurrences if active multiple days a week" {
                    calendar.add(Calendar.DATE, -23)
                    daily.startDate = calendar.time
                    daily.repeat = Days(t = true, f = true, s = true, default = false)
                    daily.everyX = 1

                    reminder.time = fakeZDT?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                    val occurrences = daily.getNextReminderOccurrences(reminder, 4, fakeZDT)
                    occurrences?.size shouldBe 4
                    // One day in the future since the time for today is already too late
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

                    reminder.time = fakeZDT?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                    val occurrences = daily.getNextReminderOccurrences(reminder, 4, fakeZDT)
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

                    reminder.time = fakeZDT?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                    val occurrences = daily.getNextReminderOccurrences(reminder, 4, fakeZDT)
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
                    val storedTime = calendar.time
                    calendar.set(Calendar.YEAR, 2024)
                    calendar.set(Calendar.MONTH, Calendar.DECEMBER)
                    calendar.set(Calendar.DAY_OF_MONTH, 17)
                    daily.startDate = calendar.time
                    daily.everyX = 1
                    daily.setWeeksOfMonth(listOf(2))
                    calendar.time = storedTime
                    if (calendar.get(Calendar.DAY_OF_MONTH) > 17) {
                        calendar.add(Calendar.MONTH, 1)
                    }

                    reminder.time = fakeZDT?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                    val occurrences = daily.getNextReminderOccurrences(reminder, 4, fakeZDT)
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

                    reminder.time = fakeZDT?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                    val occurrences = daily.getNextReminderOccurrences(reminder, 4, fakeZDT)
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

                "handle edge case for months with fewer days (e.g., February 30th)" {
                    calendar.set(Calendar.MONTH, Calendar.JANUARY)
                    calendar.set(Calendar.DAY_OF_MONTH, 30)
                    if (calendar.before(Calendar.getInstance())) {
                        calendar.add(Calendar.YEAR, 1)
                    }
                    daily.startDate = calendar.time
                    daily.everyX = 1
                    daily.setDaysOfMonth(listOf(30))

                    reminder.time = fakeZDT?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                    val occurrences = daily.getNextReminderOccurrences(reminder, 12, fakeZDT)
                    occurrences?.size shouldBe 12
                    
                    occurrences?.forEach { occurrence ->
                        val maxDayInMonth = occurrence.month.length(occurrence.toLocalDate().isLeapYear)
                        if (maxDayInMonth >= 30) {
                            occurrence.dayOfMonth shouldBe 30
                        } else {
                            occurrence.dayOfMonth shouldBe maxDayInMonth
                        }
                    }
                    
                    val febOccurrences = occurrences?.filter { it.monthValue == 2 } ?: emptyList()
                    febOccurrences.size shouldBe 1
                    
                    val febOccurrence = febOccurrences.firstOrNull()
                    febOccurrence shouldNotBe null
                    val year = febOccurrence?.year ?: 0
                    val expectedFebDays = if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
                    febOccurrence?.dayOfMonth shouldBe expectedFebDays
                }

                "ensure mutual exclusivity between daysOfMonth and weeksOfMonth" {
                    daily.setDaysOfMonth(listOf(15))
                    daily.getDaysOfMonth() shouldBe listOf(15)
                    daily.getWeeksOfMonth() shouldBe emptyList()

                    daily.setWeeksOfMonth(listOf(2))
                    daily.getWeeksOfMonth() shouldBe listOf(2)
                    daily.getDaysOfMonth() shouldBe emptyList()

                    daily.setDaysOfMonth(listOf(20))
                    daily.getDaysOfMonth() shouldBe listOf(20)
                    daily.getWeeksOfMonth() shouldBe emptyList()
                }

                "correctly handle day-of-month scheduling regardless of start date weekday" {
                    calendar.set(Calendar.YEAR, 2025)
                    calendar.set(Calendar.MONTH, Calendar.JULY)
                    calendar.set(Calendar.DAY_OF_MONTH, 12)
                    daily.startDate = calendar.time
                    daily.everyX = 1
                    daily.setDaysOfMonth(listOf(12))

                    reminder.time = fakeZDT?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                    val occurrences = daily.getNextReminderOccurrences(reminder, 3, fakeZDT)
                    occurrences?.size shouldBe 3
                    
                    occurrences?.forEach {
                        it.dayOfMonth shouldBe 12
                    }
                    
                    val weekdays = occurrences?.map { it.dayOfWeek }?.distinct() ?: emptyList()
                    (weekdays.size > 1) shouldBe true
                }
            }

            "dailies repeating yearly" should {
                beforeEach {
                    daily.frequency = Frequency.YEARLY
                }
                "return occurrences if active every year" {
                    daily.startDate = calendar.time
                    daily.everyX = 1
                    reminder.time = fakeZDT?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                    val occurrences = daily.getNextReminderOccurrences(reminder, 4, fakeZDT)
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
                    reminder.time = fakeZDT?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                    calendar.add(Calendar.YEAR, 3)
                    val occurrences = daily.getNextReminderOccurrences(reminder, 4, fakeZDT)
                    occurrences?.size shouldBe 4
                    occurrences?.forEach {
                        it.dayOfYear shouldBe calendar.get(Calendar.DAY_OF_YEAR)
                        it.year shouldBe calendar.get(Calendar.YEAR)
                        calendar.add(Calendar.YEAR, 3)
                    }
                }
            }
        }
    }
})
