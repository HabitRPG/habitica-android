package com.habitrpg.common.habitica.extensions

import com.habitrpg.common.habitica.R
import com.habitrpg.shared.habitica.models.tasks.TaskDifficulty

val TaskDifficulty.nameRes: Int
    get() = when (this) {
        TaskDifficulty.TRIVIAL -> R.string.trivial
        TaskDifficulty.EASY -> R.string.easy
        TaskDifficulty.MEDIUM -> R.string.medium
        TaskDifficulty.HARD -> R.string.hard
    }