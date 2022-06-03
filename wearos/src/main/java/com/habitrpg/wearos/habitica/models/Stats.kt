package com.habitrpg.wearos.habitica.models

import com.habitrpg.common.habitica.models.AvatarStats
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class Stats: AvatarStats {
    override val buffs: Buffs? = null
    override var habitClass: String? = null
    override var points: Int? = null
    override var lvl: Int? = null
    override var gp: Double? = null
    override var exp: Double? = null
    override var mp: Double? = null
    override var hp: Double? = null
    override var toNextLevel: Int? = null
    override var maxHealth: Int? = null
    override var maxMP: Int? = null
}