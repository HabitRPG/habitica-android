package com.habitrpg.common.habitica.models.tasks

enum class TaskType constructor(val value: String) {
    HABIT("habit"),
    DAILY("daily"),
    TODO("todo"),
    REWARD("reward");

    override fun toString(): String = value

    companion object {
        fun from(type: String?): TaskType? = values().find { it.value == type }
    }
}