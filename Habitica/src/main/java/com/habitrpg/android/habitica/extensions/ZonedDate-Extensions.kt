package com.habitrpg.android.habitica.extensions

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.TextStyle
import java.time.temporal.TemporalAccessor
import java.util.Locale

fun String.parseToZonedDateTime(): ZonedDateTime? {
    val parsed: TemporalAccessor = formatter().parseBest(
        this,
        ZonedDateTime::from, LocalDateTime::from
    )
    return if (parsed is ZonedDateTime) {
        parsed
    } else {
        val defaultZone: ZoneId = ZoneId.of("UTC")
        (parsed as LocalDateTime).atZone(defaultZone)
    }
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