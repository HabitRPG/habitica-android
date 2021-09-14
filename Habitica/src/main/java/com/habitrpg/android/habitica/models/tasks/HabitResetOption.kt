package com.habitrpg.android.habitica.models.tasks

import androidx.annotation.StringRes
import com.habitrpg.android.habitica.R

enum class HabitResetOption(val value: String, @StringRes val nameRes: Int) {
    DAILY("daily", R.string.repeat_daily),
    WEEKLY("weekly", R.string.weekly),
    MONTHLY("monthly", R.string.monthly)
}
