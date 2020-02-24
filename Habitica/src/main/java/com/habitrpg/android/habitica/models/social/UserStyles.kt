package com.habitrpg.android.habitica.models.social

import com.habitrpg.shared.habitica.models.Avatar
import com.habitrpg.shared.habitica.models.user.Items
import com.habitrpg.shared.habitica.models.user.Outfit
import com.habitrpg.shared.habitica.models.user.Preferences
import com.habitrpg.shared.habitica.models.user.Stats
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
        get(): String? {
            return items?.currentMount
        }
    override val currentPet: String?
        get(): String? {
            return items?.currentPet
        }
    override val sleep: Boolean
        get(): Boolean {
            return false
        }
    override val gemCount: Int?
        get(): Int? {
            return 0
        }
    override val hourglassCount: Int?
        get(): Int? {
            return 0
        }
    override val costume: Outfit?
        get(): Outfit? {
            return items?.gear?.costume
        }

    override val equipped: Outfit?
        get(): Outfit? {
            return items?.gear?.equipped
        }

    override fun hasClass(): Boolean {
        return false
    }

    override var stats: Stats? = null
    override var preferences: Preferences? = null
    private var items: Items? = null
}
