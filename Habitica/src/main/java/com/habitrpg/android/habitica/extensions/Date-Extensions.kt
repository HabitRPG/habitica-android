package com.habitrpg.android.habitica.extensions

import android.content.res.Resources
import com.habitrpg.android.habitica.R
import java.util.*
import kotlin.math.round
import kotlin.time.*

class DateUtils {

    companion object {

        fun createDate(year: Int, month: Int, day: Int): Date {
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
    }
}

fun Date.getAgoString(res: Resources): String {
    return this.time.getAgoString(res)
}

fun Long.getAgoString(res: Resources): String {
    val diff = Date().time - this

    val diffMinutes = diff / (60 * 1000) % 60
    val diffHours = diff / (60 * 60 * 1000) % 24
    val diffDays = diff / (24 * 60 * 60 * 1000)
    val diffWeeks = diffDays / 7
    val diffMonths = diffDays / 30

    return when {
        diffMonths != 0L -> if (diffMonths == 1L) {
            res.getString(R.string.ago_1month)
        } else res.getString(R.string.ago_months, diffMonths)
        diffWeeks != 0L -> if (diffWeeks == 1L) {
            res.getString(R.string.ago_1week)
        } else res.getString(R.string.ago_weeks, diffWeeks)
        diffDays != 0L -> if (diffDays == 1L) {
            res.getString(R.string.ago_1day)
        } else res.getString(R.string.ago_days, diffDays)
        diffHours != 0L -> if (diffHours == 1L) {
            res.getString(R.string.ago_1hour)
        } else res.getString(R.string.ago_hours, diffHours)
        diffMinutes == 1L -> res.getString(R.string.ago_1Minute)
        else -> res.getString(R.string.ago_minutes, diffMinutes)
    }
}

fun Date.getRemainingString(res: Resources): String {
    return this.time.getRemainingString(res)
}

@OptIn(ExperimentalTime::class)
fun Long.getRemainingString(res: Resources): String {
    val diff = (this - Date().time).milliseconds

    val diffMinutes = diff.inMinutes
    val diffHours = diff.inHours
    val diffDays = diff.inDays
    val diffWeeks = diffDays / 7f
    val diffMonths = diffDays / 30f

    return when {
        diffMonths != 0.0 -> if (round(diffMonths) == 1.0) {
            res.getString(R.string.remaining_1month)
        } else res.getString(R.string.remaining_months, round(diffMonths).toInt())
        diffWeeks != 0.0 -> if (round(diffWeeks) == 1.0) {
            res.getString(R.string.remaining_1week)
        } else res.getString(R.string.remaining_weeks, round(diffWeeks).toInt())
        diffDays != 0.0 -> if (diffDays == 1.0) {
            res.getString(R.string.remaining_1day)
        } else res.getString(R.string.remaining_days, diffDays)
        diffHours != 0.0 -> if (diffHours == 1.0) {
            res.getString(R.string.remaining_1hour)
        } else res.getString(R.string.remaining_hours, diffHours)
        diffMinutes == 1.0 -> res.getString(R.string.remaining_1Minute)
        else -> res.getString(R.string.remaining_minutes, diffMinutes)
    }
}

fun Date.getShortRemainingString(): String {
    return time.getShortRemainingString()
}

@OptIn(ExperimentalTime::class)
fun Long.getShortRemainingString(): String {
    var diff = Duration.milliseconds((this - Date().time))

    val diffDays = diff.toInt(DurationUnit.DAYS)
    diff -= Duration.days(diffDays)
    val diffHours = diff.toInt(DurationUnit.HOURS)
    diff -= Duration.hours(diffHours)
    val diffMinutes = diff.toInt(DurationUnit.MINUTES)
    diff -= Duration.minutes(diffMinutes)
    val diffSeconds = diff.toInt(DurationUnit.SECONDS)

    var str = "${diffMinutes}m"
    if (diffHours > 0) {
        str = "${diffHours}h $str"
    }
    if (diffDays > 0) {
        str = "${diffDays}d $str"
    }
    if (diffDays == 0 && diffHours == 0) {
        str = "$str ${diffSeconds}s"
    }
    return str
}
