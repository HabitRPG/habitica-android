package com.habitrpg.wearos.habitica.models.user

import com.habitrpg.shared.habitica.models.AvatarBuffs
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class Buffs : AvatarBuffs {
    override var con: Float? = null
    override var str: Float? = null
    override var per: Float? = null
    override var _int: Float? = null
    override var seafoam: Boolean? = null
    override var spookySparkles: Boolean? = null
    override var shinySeed: Boolean? = null
    override var snowball: Boolean? = null
    override var streaks: Boolean? = null
}
