package com.habitrpg.android.habitica.models.social

import com.habitrpg.android.habitica.models.user.Authentication
import com.habitrpg.android.habitica.models.user.Flags
import com.habitrpg.android.habitica.models.user.Items
import com.habitrpg.android.habitica.models.user.Outfit
import com.habitrpg.android.habitica.models.user.Preferences
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.shared.habitica.models.Avatar
import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class UserStyles : RealmObject(), Avatar {
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

    override val hasClass: Boolean
        get() {
            return false
        }
    override var balance: Double = 0.0
    override var authentication: Authentication? = null
    override var stats: Stats? = null
    override var preferences: Preferences? = null
    override var flags: Flags? = null
    override var items: Items? = null
}
