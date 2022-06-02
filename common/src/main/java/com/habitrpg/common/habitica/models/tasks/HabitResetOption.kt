package com.habitrpg.common.habitica.models.tasks

import androidx.annotation.StringRes
import com.habitrpg.common.habitica.R

enum class HabitResetOption(val value: Frequency, @StringRes val nameRes: Int) {
    DAILY(Frequency.DAILY, R.string.repeat_daily),
    WEEKLY(Frequency.WEEKLY, R.string.weekly),
    MONTHLY(Frequency.MONTHLY, R.string.monthly);

    companion object {
        fun from(type: Frequency?): HabitResetOption? = values().find { it.value == type }
    }
}