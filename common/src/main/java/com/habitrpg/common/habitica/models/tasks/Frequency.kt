package com.habitrpg.common.habitica.models.tasks

enum class Frequency constructor(val value: String) {
    WEEKLY("weekly"),
    DAILY("daily"),
    MONTHLY("monthly"),
    YEARLY("yearly");

    companion object {
        fun from(type: String?): Frequency? = values().find { it.value == type }
    }
}