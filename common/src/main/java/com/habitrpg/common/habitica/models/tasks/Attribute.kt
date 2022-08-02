package com.habitrpg.common.habitica.models.tasks

enum class Attribute constructor(val value: String) {
    STRENGTH("str"),
    INTELLIGENCE("int"),
    CONSTITUTION("con"),
    PERCEPTION("per");

    companion object {
        fun from(type: String?): Attribute? = values().find { it.value == type }
    }
}
