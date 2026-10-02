package com.habitrpg.android.habitica.extensions

import com.habitrpg.android.habitica.models.tasks.Days
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.Date

class ZonedDateExtensionsTest :
    WordSpec({
        "parseToZonedDateTime" should {
            "parse ISO dates with offset" {
                val parsed = "2026-03-05T10:15:30+0200".parseToZonedDateTime()
                parsed?.toLocalDateTime().toString() shouldBe "2026-03-05T10:15:30"
                parsed?.offset shouldBe ZoneOffset.ofHours(2)
            }

            "parse dates with a space separator" {
                val parsed = "2026-03-05 10:15:30Z".parseToZonedDateTime()
                parsed?.toInstant() shouldBe ZonedDateTime.of(2026, 3, 5, 10, 15, 30, 0, ZoneOffset.UTC).toInstant()
            }

            "parse fractional seconds" {
                val parsed = "2026-03-05T10:15:30.250Z".parseToZonedDateTime()
                parsed?.nano shouldBe 250_000_000
            }

            "assume UTC for dates without an offset" {
                val parsed = "2026-03-05T10:15:30".parseToZonedDateTime()
                parsed?.zone shouldBe ZoneId.of("UTC")
                parsed?.hour shouldBe 10
            }
        }

        "toZonedDateTime" should {
            "keep the instant and use the system zone" {
                val date = Date(1_700_000_000_000L)
                val zoned = date.toZonedDateTime()
                zoned?.toInstant()?.toEpochMilli() shouldBe 1_700_000_000_000L
                zoned?.zone shouldBe ZoneId.systemDefault()
            }
        }

        // 2026-03-02 is a monday
        val monday = LocalDate.of(2026, 3, 2)
        val week = (0L..6L).map { monday.plusDays(it) }

        fun onlyOn(day: DayOfWeek) =
            Days().apply {
                m = day == DayOfWeek.MONDAY
                t = day == DayOfWeek.TUESDAY
                w = day == DayOfWeek.WEDNESDAY
                th = day == DayOfWeek.THURSDAY
                f = day == DayOfWeek.FRIDAY
                s = day == DayOfWeek.SATURDAY
                su = day == DayOfWeek.SUNDAY
            }

        "ZonedDateTime.matchesRepeatDays" should {
            "match every day without repeat days" {
                week.forEach { it.atStartOfDay(ZoneOffset.UTC).matchesRepeatDays(null) shouldBe true }
            }

            "match only the selected weekday" {
                DayOfWeek.entries.forEach { selected ->
                    val days = onlyOn(selected)
                    week.forEach { date ->
                        date.atStartOfDay(ZoneOffset.UTC).matchesRepeatDays(days) shouldBe (date.dayOfWeek == selected)
                    }
                }
            }
        }

        "Calendar.matchesRepeatDays" should {
            fun calendarOf(date: LocalDate) =
                Calendar.getInstance().apply {
                    clear()
                    set(date.year, date.monthValue - 1, date.dayOfMonth)
                }

            "match every day without repeat days" {
                week.forEach { calendarOf(it).matchesRepeatDays(null) shouldBe true }
            }

            "match only the selected weekday" {
                DayOfWeek.entries.forEach { selected ->
                    val days = onlyOn(selected)
                    week.forEach { date ->
                        calendarOf(date).matchesRepeatDays(days) shouldBe (date.dayOfWeek == selected)
                    }
                }
            }
        }
    })
