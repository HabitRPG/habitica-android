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
class Profile {
    var name: String? = null
}

@JsonClass(generateAdapter = true)
class User: Avatar {
    override var currentMount: String? = null
    override var currentPet: String? = null
    override var sleep: Boolean = false
    override var stats: Stats? = null
    override var preferences: Preferences? = null
    override var flags: Flags? = null
    override var gemCount: Int = 0
    override var hourglassCount: Int = 0
    var items: Items? = null
    override val costume: Outfit?
        get() = items?.gear?.costume
    override val equipped: Outfit?
        get() = items?.gear?.equipped
    override val hasClass: Boolean = false

    var profile: Profile? = null

    override fun isValid(): Boolean {
        return true
    }
}