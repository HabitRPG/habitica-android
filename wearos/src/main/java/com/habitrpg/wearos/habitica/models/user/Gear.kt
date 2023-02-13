package com.habitrpg.wearos.habitica.models.user

import com.habitrpg.shared.habitica.models.AvatarGear
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class Gear : AvatarGear {
    override var equipped: Outfit? = null
    override var costume: Outfit? = null
}
