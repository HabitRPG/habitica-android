package com.habitrpg.android.habitica.extensions

import android.content.res.Resources
import com.habitrpg.android.habitica.R
import java.util.*
import kotlin.math.round

class DateUtils {

    companion object {
        private fun minutesInMs(minutes: Int): Int {
            return minutes * 60 * 1000
        }

        fun hoursInMs(hours: Int): Int {
            return hours * minutesInMs(60)
        }

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

fun Long.getRemainingString(res: Resources): String {
    val diff = this - Date().time

    val diffMinutes = diff / (60 * 1000) % 60
    val diffHours = diff / (60 * 60 * 1000) % 24
    val diffDays = diff / (24 * 60 * 60 * 1000)
    val diffWeeks = diffDays / 7f
    val diffMonths = diffDays / 30f

    return when {
        diffMonths != 0f -> if (round(diffMonths) == 1f) {
            res.getString(R.string.remaining_1month)
        } else res.getString(R.string.remaining_months, round(diffMonths).toInt())
        diffWeeks != 0f -> if (round(diffWeeks) == 1f) {
            res.getString(R.string.remaining_1week)
        } else res.getString(R.string.remaining_weeks, round(diffWeeks).toInt())
        diffDays != 0L -> if (diffDays == 1L) {
            res.getString(R.string.remaining_1day)
        } else res.getString(R.string.remaining_days, diffDays)
        diffHours != 0L -> if (diffHours == 1L) {
            res.getString(R.string.remaining_1hour)
        } else res.getString(R.string.remaining_hours, diffHours)
        diffMinutes == 1L -> res.getString(R.string.remaining_1Minute)
        else -> res.getString(R.string.remaining_minutes, diffMinutes)
    }
}