package com.habitrpg.shared.habitica.models

interface AvatarStats {
    val buffs: AvatarBuffs?
    var habitClass: String?
    var points: Int?
    var lvl: Int?
    var gp: Double?
    var exp: Double?
    var mp: Double?
    var hp: Double?

    var toNextLevel: Int?
    var maxHealth: Int?
    var maxMP: Int?
    val isBuffed: Boolean
        get() {
            return (buffs?.str ?: 0f) > 0 ||
                (buffs?.con ?: 0f) > 0 ||
                (buffs?._int ?: 0f) > 0 ||
                (buffs?.per ?: 0f) > 0
        }
}
