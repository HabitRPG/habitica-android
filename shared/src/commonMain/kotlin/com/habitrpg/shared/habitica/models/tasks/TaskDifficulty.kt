package com.habitrpg.shared.habitica.models.tasks

enum class TaskDifficulty(val value: Float) {
    TRIVIAL(0.1f),
    EASY(1f),
    MEDIUM(1.5f),
    HARD(2f);

    companion object {
        fun valueOf(float: Float): TaskDifficulty {
            return when (float) {
                0.1f -> TRIVIAL
                1.0f -> EASY
                1.5f -> MEDIUM
                2f -> HARD
                else -> MEDIUM
            }
        }
    }
}
