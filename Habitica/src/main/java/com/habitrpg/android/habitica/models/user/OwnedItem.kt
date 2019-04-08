package com.habitrpg.android.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class OwnedItem : RealmObject() {

    @PrimaryKey
    var combinedKey: String? = null
    var userID: String? = null
    set(value) {
        field = value
        combinedKey = field + key
    }
    var key: String? = null
    set(value) {
        field = value
        combinedKey = field + key
    }

    var itemType: String? = null
    var numberOwned = 0
}
