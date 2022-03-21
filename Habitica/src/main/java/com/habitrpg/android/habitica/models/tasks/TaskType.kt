package com.habitrpg.android.habitica.models.tasks

enum class TaskType constructor(val value: String) {
    HABIT("habit"),
    DAILY("daily"),
    TODO("todo"),
    REWARD("reward"),
    ADD_ITEM("ADD_ITEM");

    override fun toString(): String = value

    companion object {
        fun from(type: String?): TaskType? = values().find { it.value == type }
    }
}
