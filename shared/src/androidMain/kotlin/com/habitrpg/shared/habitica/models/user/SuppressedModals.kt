package com.habitrpg.shared.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class SuppressedModals: RealmObject() {
    @PrimaryKey
    actual var userId: String? = null

    internal actual var preferences: Preferences? = null
    actual var streak: Boolean? = null
    actual var raisePet: Boolean? = null
    actual var hatchPet: Boolean? = null
    actual var levelUp: Boolean? = null
}