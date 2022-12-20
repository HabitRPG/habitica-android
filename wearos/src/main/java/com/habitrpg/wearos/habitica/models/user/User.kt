package com.habitrpg.wearos.habitica.models.user

import com.habitrpg.shared.habitica.models.Avatar
import com.habitrpg.shared.habitica.models.AvatarAuthentication
import com.habitrpg.shared.habitica.models.tasks.TasksOrder
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class User: Avatar {
    val tasksOrder: TasksOrder? = null
    val isDead: Boolean
    get() = (stats?.hp ?: 0.0) <= 0.0
    override val currentMount: String?
        get() = items?.currentMount
    override val currentPet: String?
        get() = items?.currentPet
    override var sleep: Boolean = false
    override var id: String? = null
    override var balance: Double = 0.0
    @Json(ignore = true)
    override var authentication: AvatarAuthentication? = null
    override var stats: Stats? = null
    override var preferences: Preferences? = null
    override var flags: Flags? = null
    override var gemCount: Int = 0
    override var hourglassCount: Int = 0
    override var items: Items? = null
    override val costume: Outfit?
        get() = items?.gear?.costume
    override val equipped: Outfit?
        get() = items?.gear?.equipped
    override val hasClass: Boolean = false

    var needsCron: Boolean = false

    var profile: Profile? = null

    override fun isValid(): Boolean {
        return true
    }
}