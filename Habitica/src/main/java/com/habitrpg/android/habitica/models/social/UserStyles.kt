package com.habitrpg.android.habitica.models.social

import com.habitrpg.shared.habitica.Avatar
import com.habitrpg.shared.habitica.models.user.*
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class UserStyles : RealmObject(), Avatar {
    @PrimaryKey
    var id: String? = null
        set(value) {
            field = value
            stats?.userId = id
            preferences?.userId = id
            items?.userId = id
        }

    var items: Items? = null

    override val currentMount: String?
        get() = items?.currentMount

    override val currentPet: String?
        get() = items?.currentPet

    override var sleep: Boolean = false

    override var stats: Stats? = null

    override var preferences: AvatarPreferences? = null

    override val gemCount: Int?
        get() = 0

    override val hourglassCount: Int?
        get() = 0

    override val costume: Outfit?
        get() = items?.gear?.costume
    override val equipped: Outfit?
        get() = items?.gear?.equipped

    override var valid: Boolean = true

    override fun hasClass(): Boolean {
        return false
    }

}
