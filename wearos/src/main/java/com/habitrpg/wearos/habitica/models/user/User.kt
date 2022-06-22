package com.habitrpg.wearos.habitica.models.user

import com.habitrpg.common.habitica.models.Avatar
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class User: Avatar {
    val isDead: Boolean
    get() = (stats?.hp ?: 0.0) <= 0.0
    override val currentMount: String?
        get() = items?.currentMount
    override val currentPet: String?
        get() = items?.currentPet
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