package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.models.HabitRpgClass
import com.habitrpg.shared.habitica.nativeLibraries.NativeContext


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

