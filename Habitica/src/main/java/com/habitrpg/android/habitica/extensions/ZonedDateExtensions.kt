package com.habitrpg.android.habitica.extensions

import com.habitrpg.android.habitica.models.tasks.Days
import com.habitrpg.shared.habitica.models.tasks.Frequency
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAccessor
import java.util.Date
import java.util.Locale

fun String.parseToZonedDateTime(): ZonedDateTime? {
    val parsed: TemporalAccessor = formatter().parseBest(
        this,
        ZonedDateTime::from,
        LocalDateTime::from
    )
    return if (parsed is ZonedDateTime) {
        parsed
    } else {
        val defaultZone: ZoneId = ZoneId.of("UTC")
        (parsed as LocalDateTime).atZone(defaultZone)
    }
}

fun Date.toZonedDateTime(): ZonedDateTime? {
    return this.toInstant().atZone(ZoneId.systemDefault())
}

/**
 * Returns full display name in default Locale (Monday, Tuesday, Wednesday, etc.)
 */
fun ZonedDateTime.dayOfWeekString(): String {
    return DayOfWeek.from(this).getDisplayName(TextStyle.FULL, Locale.getDefault())
}

fun formatter(): DateTimeFormatter =
    DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_LOCAL_DATE)
        .appendPattern("['T'][' ']")
        .append(DateTimeFormatter.ISO_LOCAL_TIME)
        .appendPattern("[XX]")
        .toFormatter()


fun ZonedDateTime.matchesDailyInterval(startDate: ZonedDateTime, everyX: Int): Boolean {
    val daysBetween = ChronoUnit.DAYS.between(startDate.toLocalDate(), this.toLocalDate())
    return daysBetween % everyX == 0L
}

fun ZonedDateTime.matchesWeeklyInterval(startDate: ZonedDateTime, everyX: Int): Boolean {
    val weeksBetween = ChronoUnit.WEEKS.between(startDate.toLocalDate(), this.toLocalDate())
    return weeksBetween % everyX == 0L
}

fun ZonedDateTime.matchesMonthlyInterval(startDate: ZonedDateTime, everyX: Int, dayOfMonth: Int): Boolean {
    val monthsBetween = ChronoUnit.MONTHS.between(startDate.toLocalDate(), this.toLocalDate())
    return this.dayOfMonth == dayOfMonth && monthsBetween % everyX == 0L
}

fun ZonedDateTime.matchesYearlyInterval(startDate: ZonedDateTime, everyX: Int): Boolean {
    val yearsBetween = ChronoUnit.YEARS.between(startDate.toLocalDate(), this.toLocalDate())
    return yearsBetween % everyX == 0L
}

fun ZonedDateTime.matchesRepeatDays(repeatDays: Days?): Boolean {
    repeatDays ?: return true // If no repeatDays specified, assume it matches

    return when (this.dayOfWeek) {
        DayOfWeek.MONDAY -> repeatDays.m
        DayOfWeek.TUESDAY -> repeatDays.t
        DayOfWeek.WEDNESDAY -> repeatDays.w
        DayOfWeek.THURSDAY -> repeatDays.th
        DayOfWeek.FRIDAY -> repeatDays.f
        DayOfWeek.SATURDAY -> repeatDays.s
        DayOfWeek.SUNDAY -> repeatDays.su
        else -> false
    }
}


// Probably shouldn't be an extension function, but it's easier to test this way
fun ZonedDateTime.isReminderDue(reminderTime: ZonedDateTime, frequency: Frequency, everyX: Int, repeatDays: Days?, dayOfMonth: Int): Boolean {
    // Check if the reminder is due based on the frequency and everyX
    when (frequency) {
        Frequency.DAILY -> {
            if (!this.matchesDailyInterval(reminderTime, everyX)) return false
        }
        Frequency.WEEKLY -> {
            if (!this.matchesWeeklyInterval(reminderTime, everyX)) return false
        }
        Frequency.MONTHLY -> {
            if (!this.matchesMonthlyInterval(reminderTime, everyX, dayOfMonth)) return false
        }
        Frequency.YEARLY -> {
            if (!this.matchesYearlyInterval(reminderTime, everyX)) return false
        }
    }

    // Check if the reminder is due based on the repeatDays
    return this.matchesRepeatDays(repeatDays)
}

