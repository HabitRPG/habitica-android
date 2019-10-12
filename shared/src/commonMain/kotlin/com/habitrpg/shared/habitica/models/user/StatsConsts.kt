package com.habitrpg.shared.habitica.models.user

class StatsConsts: Stats() {
    companion object {
        const val STRENGTH = "str"
        const val INTELLIGENCE = "int"
        const val CONSTITUTION = "con"
        const val PERCEPTION = "per"


        const val WARRIOR = "warrior"
        const val MAGE = "wizard"
        const val HEALER = "healer"
        const val ROGUE = "rogue"

        const val AUTO_ALLOCATE_FLAT = "flat"
        const val AUTO_ALLOCATE_CLASSBASED = "classbased"
        const val AUTO_ALLOCATE_TASKBASED = "taskbased"
    }
}