package com.habitrpg.android.habitica.models.social

import com.habitrpg.android.habitica.models.Avatar
import com.habitrpg.android.habitica.models.AvatarPreferences
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
    override fun getCurrentMount(): String? {
        return items?.currentMount
    }

    override fun getCurrentPet(): String? {
        return items?.currentPet
    }

    override fun getSleep(): Boolean {
        return false
    }

    override fun getStats(): Stats? {
        return stats
    }

    override fun getPreferences(): AvatarPreferences? {
        return preferences
    }

    override fun getGemCount(): Int {
        return 0
    }

    override fun getHourglassCount(): Int {
        return 0
    }

    override fun getCostume(): Outfit? {
        return items?.gear?.costume
    }

    override fun getEquipped(): Outfit? {
        return items?.gear?.equipped
    }

    override fun hasClass(): Boolean {
        return false
    }

    private var stats: Stats? = null
    private var preferences: Preferences? = null
    private var items: Items? = null
}
