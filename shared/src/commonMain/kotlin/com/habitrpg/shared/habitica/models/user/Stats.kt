package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.models.HabitRpgClass
import com.habitrpg.shared.habitica.nativeLibraries.NativeContext

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

expect open class Stats() {
    var userId: String?

    internal var user: User?
    var constitution: Int?
    var strength: Int?
    var per: Int?
    var intelligence: Int?
    var training: Training?
    var buffs: Buffs?
    var points: Int?
    var lvl: Int?
    var habitClass: String?
    var gp: Double?
    var exp: Double?
    var mp: Double?
    var hp: Double?
    var toNextLevel: Int?
    var maxHealth: Int?
    var maxMP: Int?
    val isBuffed: Boolean

    fun getTranslatedClassName(context: NativeContext): String

    fun merge(stats: Stats?)

    fun setHabitClass(habitRpgClass: HabitRpgClass)
}

