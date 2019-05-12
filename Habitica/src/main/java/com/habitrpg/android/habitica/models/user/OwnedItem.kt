package com.habitrpg.android.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class OwnedItem : RealmObject(), OwnedObject {

    @PrimaryKey
    override var combinedKey: String? = null
    override var userID: String? = null
    set(value) {
        field = value
        combinedKey = field + itemType + key
    }
    override var key: String? = null
    set(value) {
        field = value
        combinedKey = userID + itemType + field
    }

    var itemType: String? = null
        set(value) {
            field = value
            combinedKey = userID + field + key
        }
    var numberOwned = 0
}
