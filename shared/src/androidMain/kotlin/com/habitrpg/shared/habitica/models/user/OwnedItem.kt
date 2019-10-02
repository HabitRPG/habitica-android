package com.habitrpg.shared.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class OwnedItem : RealmObject(), OwnedObject {

    @PrimaryKey
    actual override var combinedKey: String? = null
    actual override var userID: String? = null
    set(value) {
        field = value
        combinedKey = field + itemType + key
    }
    actual override var key: String? = null
    set(value) {
        field = value
        combinedKey = userID + itemType + field
    }

    actual var itemType: String? = null
        set(value) {
            field = value
            combinedKey = userID + field + key
        }
    actual var numberOwned = 0
}
