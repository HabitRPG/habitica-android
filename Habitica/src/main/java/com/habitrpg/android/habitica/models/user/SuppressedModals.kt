package com.habitrpg.android.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class SuppressedModals : RealmObject() {
    @PrimaryKey
    var userId: String? = null
    var preferences: Preferences? = null
    var streak: Boolean? = null
    var raisePet: Boolean? = null
    var hatchPet: Boolean? = null
    var levelUp: Boolean? = null
}