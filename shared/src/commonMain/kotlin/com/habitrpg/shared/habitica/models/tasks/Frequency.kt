package com.habitrpg.shared.habitica.models.tasks

enum class Frequency(val value: String) {
    WEEKLY("semanal"),
    DAILY("diario"),
    MONTHLY("mensal"),
    YEARLY("anual")
    ;

    companion object {
        fun from(type: String?): Frequency? = values().find { it.value == type }
    }
}
