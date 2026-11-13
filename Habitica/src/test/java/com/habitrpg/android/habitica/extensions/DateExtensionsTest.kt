package com.habitrpg.android.habitica.extensions

import android.content.res.Resources
import com.habitrpg.android.habitica.R
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.util.Calendar
import java.util.Date
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

class DateExtensionsTest :
    WordSpec({
        // Resolves resources to their names, so results read like "ago_days(3)"
        val stringNames = R.string::class.java.fields.associate { it.getInt(null) to it.name }
        val pluralNames = R.plurals::class.java.fields.associate { it.getInt(null) to it.name }
        val res = mockk<Resources>()
        every { res.getString(any()) } answers { stringNames.getValue(firstArg()) }
        every { res.getString(any(), *anyVararg()) } answers {
            val formatArgs = args.drop(1).flatMap { if (it is Array<*>) it.toList() else listOf(it) }
            "${stringNames.getValue(firstArg())}(${formatArgs.joinToString()})"
        }
        every { res.getQuantityString(any(), any(), *anyVararg()) } answers {
            "${pluralNames.getValue(firstArg())}(${secondArg<Int>()})"
        }

        // Small margin so the time passing during the test does not cross a unit boundary
        val margin = 30.seconds.inWholeMilliseconds

        fun ago(duration: kotlin.time.Duration) = Date().time - duration.inWholeMilliseconds - margin

        fun inFuture(duration: kotlin.time.Duration) = Date().time + duration.inWholeMilliseconds + margin

        "getShortRemainingString" should {
            "contain day if multiple days" {
                (Date().time + 2077400000L).getShortRemainingString() shouldBe "24d 1h 3m"
                (Date().time + 2091600500L).getShortRemainingString() shouldBe "24d 5h"
                (Date().time + 2074200500L).getShortRemainingString() shouldBe "24d 10m"
                (Date().time + 2073600500L).getShortRemainingString() shouldBe "24d"
            }
            "contain hours if multiple hours" {
                (Date().time + 20774000L).getShortRemainingString() shouldBe "5h 46m"
                (Date().time + 82800500L).getShortRemainingString() shouldBe "23h"
            }

            "contain minutes and seconds if less than 1 hour" {
                (Date().time + 2077400L).getShortRemainingString() shouldBe "34m 37s"
                (Date().time + 2400500L).getShortRemainingString() shouldBe "40m"
            }

            "be empty for dates in the past" {
                (Date().time - 60_000L).getShortRemainingString() shouldBe ""
            }
        }

        "getAgoString" should {
            "use minutes for recent dates" {
                ago(0.minutes).getAgoString(res) shouldBe "ago_minutes(0)"
                ago(1.minutes).getAgoString(res) shouldBe "ago_1Minute"
                ago(45.minutes).getAgoString(res) shouldBe "ago_minutes(45)"
            }

            "use hours" {
                ago(1.hours).getAgoString(res) shouldBe "ago_1hour"
                ago(5.hours).getAgoString(res) shouldBe "ago_hours(5)"
            }

            "use days" {
                ago(1.days).getAgoString(res) shouldBe "ago_1day"
                ago(6.days).getAgoString(res) shouldBe "ago_days(6)"
            }

            "use weeks" {
                ago(7.days).getAgoString(res) shouldBe "ago_1week"
                ago(20.days).getAgoString(res) shouldBe "ago_weeks(2)"
            }

            "use months" {
                ago(31.days).getAgoString(res) shouldBe "ago_1month"
                ago(100.days).getAgoString(res) shouldBe "ago_months(3)"
            }

            "work with dates" {
                Date(ago(2.hours)).getAgoString(res) shouldBe "ago_hours(2)"
            }
        }

        "getRemainingString" should {
            "use minutes for close dates" {
                inFuture(1.minutes).getRemainingString(res) shouldBe "remaining_1Minute"
                inFuture(30.minutes).getRemainingString(res) shouldBe "remaining_minutes(30)"
            }

            "use hours" {
                inFuture(1.hours).getRemainingString(res) shouldBe "remaining_1hour"
                inFuture(12.hours).getRemainingString(res) shouldBe "remaining_hours(12)"
            }

            "use days" {
                inFuture(1.days).getRemainingString(res) shouldBe "remaining_1day"
                inFuture(3.days).getRemainingString(res) shouldBe "remaining_days(3)"
            }

            "use weeks" {
                inFuture(7.days).getRemainingString(res) shouldBe "remaining_1week"
                inFuture(15.days).getRemainingString(res) shouldBe "remaining_weeks(2)"
            }

            "use months" {
                inFuture(30.days).getRemainingString(res) shouldBe "remaining_1month"
                inFuture(65.days).getRemainingString(res) shouldBe "remaining_months(2)"
            }
        }

        "getImpreciseRemainingString" should {
            "only show the largest unit" {
                inFuture(3.days + 5.hours).getImpreciseRemainingString(res) shouldBe "x_days(3)"
                inFuture(5.hours + 20.minutes).getImpreciseRemainingString(res) shouldBe "x_hours(5)"
                inFuture(20.minutes).getImpreciseRemainingString(res) shouldBe "x_minutes(20)"
            }

            "show at least one minute" {
                (Date().time + 10_000L).getImpreciseRemainingString(res) shouldBe "x_minutes(1)"
                (Date().time - 60_000L).getImpreciseRemainingString(res) shouldBe "x_minutes(1)"
            }
        }

        "getMinuteOrSeconds" should {
            "use seconds below an hour" {
                59.minutes.getMinuteOrSeconds() shouldBe DurationUnit.SECONDS
            }

            "use minutes from an hour on" {
                1.hours.getMinuteOrSeconds() shouldBe DurationUnit.MINUTES
                3.days.getMinuteOrSeconds() shouldBe DurationUnit.MINUTES
            }
        }

        "DateUtils" should {
            "create dates at midnight" {
                val calendar = Calendar.getInstance().apply { time = DateUtils.createDate(2026, Calendar.MARCH, 5) }
                calendar[Calendar.YEAR] shouldBe 2026
                calendar[Calendar.MONTH] shouldBe Calendar.MARCH
                calendar[Calendar.DAY_OF_MONTH] shouldBe 5
                calendar[Calendar.HOUR_OF_DAY] shouldBe 0
                calendar[Calendar.MINUTE] shouldBe 0
                calendar[Calendar.SECOND] shouldBe 0
                calendar[Calendar.MILLISECOND] shouldBe 0
            }

            "detect dates on the same day" {
                val morning = DateUtils.createDate(2026, Calendar.MARCH, 5)
                val evening = Date(morning.time + 23.hours.inWholeMilliseconds)
                DateUtils.isSameDay(morning, evening) shouldBe true
            }

            "detect dates on different days" {
                val day = DateUtils.createDate(2026, Calendar.MARCH, 5)
                DateUtils.isSameDay(day, DateUtils.createDate(2026, Calendar.MARCH, 6)) shouldBe false
                DateUtils.isSameDay(day, DateUtils.createDate(2025, Calendar.MARCH, 5)) shouldBe false
            }
        }
    })
