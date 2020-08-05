package com.habitrpg.android.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class UserAchievement : RealmObject {

    @PrimaryKey
    var combinedKey: String? = null
    var userId: String? = null
        set(value) {
            field = value
            combinedKey = field + key
        }
    var key: String? = null
        set(value) {
            field = value
            combinedKey = userId + field
        }
    var earned: Boolean = false

    constructor()
}

