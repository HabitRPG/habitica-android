package com.habitrpg.android.habitica.models.social

import com.habitrpg.android.habitica.models.Avatar
import com.habitrpg.android.habitica.models.user.Items
import com.habitrpg.android.habitica.models.user.Outfit
import com.habitrpg.android.habitica.models.user.Preferences
import com.habitrpg.android.habitica.models.user.Stats
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

    override val currentMount: String?
        get() = items?.currentMount

    override val currentPet: String?
        get() = items?.currentPet

    override val sleep: Boolean
        get() = false

    override val gemCount: Int
        get() = 0
    override val hourglassCount: Int
        get() = 0

    override val costume: Outfit?
        get() = items?.gear?.costume

    override val equipped: Outfit?
        get() = items?.gear?.equipped

    override fun hasClass(): Boolean {
        return false
    }

    override var stats: Stats? = null
    override var preferences: Preferences? = null
    private var items: Items? = null
}
