package com.habitrpg.android.habitica.extensions

import android.content.res.Resources
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.helpers.LanguageHelper
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class DateUtils {
    companion object {
        fun createDate(
            year: Int,
            month: Int,
            day: Int,
        ): Date {
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, day)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.time
        }

        fun isSameDay(
            date1: Date,
            date2: Date,
        ): Boolean {
            val cal1 = Calendar.getInstance()
            val cal2 = Calendar.getInstance()
            cal1.time = date1
            cal2.time = date2
            return cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR] &&
                cal1[Calendar.YEAR] == cal2[Calendar.YEAR]
        }
    }
}

fun Date.getAgoString(res: Resources): String {
    return this.time.getAgoString(res)
}

fun Long.getAgoString(res: Resources): String {
    val diff = (Date().time - this).toDuration(DurationUnit.MILLISECONDS)

    val diffMinutes = diff.inWholeMinutes
    val diffHours = diff.inWholeHours
    val diffDays = diff.inWholeDays
    val diffWeeks = diffDays / 7
    val diffMonths = diffDays / 30

    return when {
        diffMonths != 0L ->
            if (diffMonths == 1L) {
                res.getString(R.string.ago_1month)
            } else {
                res.getString(R.string.ago_months, diffMonths)
            }

        diffWeeks != 0L ->
            if (diffWeeks == 1L) {
                res.getString(R.string.ago_1week)
            } else {
                res.getString(R.string.ago_weeks, diffWeeks)
            }

        diffDays != 0L ->
            if (diffDays == 1L) {
                res.getString(R.string.ago_1day)
            } else {
                res.getString(R.string.ago_days, diffDays)
            }

        diffHours != 0L ->
            if (diffHours == 1L) {
                res.getString(R.string.ago_1hour)
            } else {
                res.getString(R.string.ago_hours, diffHours)
            }

        diffMinutes == 1L -> res.getString(R.string.ago_1Minute)
        else -> res.getString(R.string.ago_minutes, diffMinutes)
    }
}

fun Date.getRemainingString(res: Resources): String {
    return this.time.getRemainingString(res)
}

fun Long.getRemainingString(res: Resources): String {
    val diff = (this - Date().time).toDuration(DurationUnit.MILLISECONDS)

    val diffMinutes = diff.inWholeMinutes
    val diffHours = diff.inWholeHours
    val diffDays = diff.inWholeDays
    val diffWeeks = diffDays / 7
    val diffMonths = diffDays / 30

    return when {
        diffMonths != 0L ->
            if (diffMonths == 1L) {
                res.getString(R.string.remaining_1month)
            } else {
                res.getString(R.string.remaining_months, diffMonths)
            }

        diffWeeks != 0L ->
            if (diffWeeks == 1L) {
                res.getString(R.string.remaining_1week)
            } else {
                res.getString(R.string.remaining_weeks, diffWeeks)
            }

        diffDays != 0L ->
            if (diffDays == 1L) {
                res.getString(R.string.remaining_1day)
            } else {
                res.getString(R.string.remaining_days, diffDays)
            }

        diffHours != 0L ->
            if (diffHours == 1L) {
                res.getString(R.string.remaining_1hour)
            } else {
                res.getString(R.string.remaining_hours, diffHours)
            }

        diffMinutes == 1L -> res.getString(R.string.remaining_1Minute)
        else -> res.getString(R.string.remaining_minutes, diffMinutes)
    }
}

fun Date.getShortRemainingString(): String {
    return time.getShortRemainingString()
}

fun Long.getShortRemainingString(): String {
    var diff = (this - Date().time).toDuration(DurationUnit.MILLISECONDS)

    val diffDays = diff.toInt(DurationUnit.DAYS)
    diff -= diffDays.toDuration(DurationUnit.DAYS)
    val diffHours = diff.toInt(DurationUnit.HOURS)
    diff -= diffHours.toDuration(DurationUnit.HOURS)
    val diffMinutes = diff.toInt(DurationUnit.MINUTES)
    diff -= diffMinutes.toDuration(DurationUnit.MINUTES)
    val diffSeconds = diff.toInt(DurationUnit.SECONDS)

    val components = mutableListOf<String>()
    if (diffMinutes > 0) {
        components.add("${diffMinutes}m")
    }
    if (diffHours > 0) {
        components.add(0, "${diffHours}h")
    }
    if (diffDays > 0) {
        components.add(0, "${diffDays}d")
    }
    if (diffDays == 0 && diffHours == 0 && diffSeconds > 0) {
        components.add("${diffSeconds}s")
    }
    return components.joinToString(" ")
}

fun Duration.getMinuteOrSeconds(): DurationUnit {
    return if (this.inWholeHours < 1) DurationUnit.SECONDS else DurationUnit.MINUTES
}

fun Date.formatForLocale(): String {
    val locale = LanguageHelper.systemLocale
    val dateFormatter: DateFormat =
        if (locale == Locale.US || locale == Locale.ENGLISH) {
            SimpleDateFormat("M/d/yy", locale)
        } else {
            SimpleDateFormat.getDateInstance(DateFormat.LONG, locale)
        }

    return dateFormatter.format(this)
}

fun Date.getImpreciseRemainingString(res: Resources): String {
    return time.getImpreciseRemainingString(res)
}

fun Long.getImpreciseRemainingString(res: Resources): String {
    var diff = (this - Date().time).toDuration(DurationUnit.MILLISECONDS)

    val diffDays = diff.toInt(DurationUnit.DAYS)
    if (diffDays > 0) {
        return res.getQuantityString(R.plurals.x_days, diffDays, diffDays)
    }
    diff -= diffDays.toDuration(DurationUnit.DAYS)
    val diffHours = diff.toInt(DurationUnit.HOURS)
    if (diffHours > 0) {
        return res.getQuantityString(R.plurals.x_hours, diffHours, diffHours)
    }
    diff -= diffHours.toDuration(DurationUnit.HOURS)
    val diffMinutes = diff.toInt(DurationUnit.MINUTES)
    return if (diffMinutes > 0) {
        res.getQuantityString(R.plurals.x_minutes, diffMinutes, diffMinutes)
    } else {
        res.getQuantityString(R.plurals.x_minutes, 1, 1)
    }
}
