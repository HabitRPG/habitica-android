package com.habitrpg.common.habitica.extensions

import com.habitrpg.common.habitica.R
import com.habitrpg.shared.habitica.models.tasks.HabitResetOption

val HabitResetOption.nameRes: Int
    get() =
        when (this) {
            HabitResetOption.DAILY -> R.string.daily
            HabitResetOption.WEEKLY -> R.string.weekly
            HabitResetOption.MONTHLY -> R.string.monthly
        }
