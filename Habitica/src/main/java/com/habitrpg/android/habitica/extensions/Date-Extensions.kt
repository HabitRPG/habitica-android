package com.habitrpg.android.habitica.extensions

import android.content.res.Resources
import com.habitrpg.android.habitica.R
import java.util.*

class DateUtils {

    companion object {
        private fun minutesInMs(minutes: Int): Int {
            return minutes * 60 * 1000
        }

        fun hoursInMs(hours: Int): Int {
            return hours * minutesInMs(60)
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
    val diffMonths = diffDays / 30

    return when {
        diffMonths != 0L -> if (diffMonths == 1L) {
            res.getString(R.string.ago_1month)
        } else res.getString(R.string.ago_months, diffMonths)
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
    val diffMonths = diffDays / 30

    return when {
        diffMonths != 0L -> if (diffMonths == 1L) {
            res.getString(R.string.remaining_1month)
        } else res.getString(R.string.remaining_months, diffMonths)
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