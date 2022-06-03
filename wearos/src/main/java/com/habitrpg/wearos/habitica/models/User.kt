package com.habitrpg.wearos.habitica.models

import com.habitrpg.common.habitica.models.Avatar
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class Gear {
    var equipped: Outfit? = null
    var costume: Outfit? = null
}

@JsonClass(generateAdapter = true)
class Items {
    var gear: Gear? = null
}

@JsonClass(generateAdapter = true)
class User: Avatar {
    override val currentMount: String? = null
    override val currentPet: String? = null
    override val sleep: Boolean = false
    override val stats: Stats? = null
    override val preferences: Preferences? = null
    override val flags: Flags? = null
    override val gemCount: Int = 0
    override val hourglassCount: Int = 0
    val items: Items? = null
    override val costume: Outfit?
        get() = items?.gear?.costume
    override val equipped: Outfit?
        get() = items?.gear?.equipped
    override val hasClass: Boolean = false

    override fun isValid(): Boolean {
        return true
    }
}