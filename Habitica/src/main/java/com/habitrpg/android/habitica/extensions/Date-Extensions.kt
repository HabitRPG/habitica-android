package com.habitrpg.android.habitica.extensions

class DateUtils {

    companion object {
        fun minutesInMs(minutes: Int): Int {
            return minutes * 60 * 1000
        }

        fun hoursInMs(hours: Int): Int {
            return hours * minutesInMs(60)
        }
    }
}



