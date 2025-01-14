package com.habitrpg.android.habitica.extensions

import com.habitrpg.android.habitica.models.tasks.Days
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.TemporalAccessor
import java.util.Calendar
import java.util.Date

fun String.parseToZonedDateTime(): ZonedDateTime? {
    val parsed: TemporalAccessor =
        formatter().parseBest(
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

fun formatter(): DateTimeFormatter =
    DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_LOCAL_DATE)
        .appendPattern("['T'][' ']")
        .append(DateTimeFormatter.ISO_LOCAL_TIME)
        .appendPattern("[XX]")
        .toFormatter()

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

fun Calendar.matchesRepeatDays(repeatDays: Days?): Boolean {
    repeatDays ?: return true // If no repeatDays specified, assume it matches

    return when (this.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> repeatDays.m
        Calendar.TUESDAY -> repeatDays.t
        Calendar.WEDNESDAY -> repeatDays.w
        Calendar.THURSDAY -> repeatDays.th
        Calendar.FRIDAY -> repeatDays.f
        Calendar.SATURDAY -> repeatDays.s
        Calendar.SUNDAY -> repeatDays.su
        else -> false
    }
}
