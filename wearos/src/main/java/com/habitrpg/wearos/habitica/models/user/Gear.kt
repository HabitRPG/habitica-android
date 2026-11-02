package com.habitrpg.wearos.habitica.models.user

import com.habitrpg.shared.habitica.models.AvatarGear
import kotlinx.serialization.Serializable

@Serializable
open class Gear : AvatarGear {
    override var equipped: Outfit? = null
    override var costume: Outfit? = null
}
