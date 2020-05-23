package com.habitrpg.android.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class OwnedCustomization : RealmObject(), OwnedObject {

    @PrimaryKey
    override var combinedKey: String? = null
    override var userID: String? = null
        set(value) {
            field = value
            combinedKey = field + type + key
        }
    override var key: String? = null
        set(value) {
            field = value
            combinedKey = userID + type + field
        }

    var type: String? = null
        set(value) {
            field = value
            combinedKey = userID + field + key
        }
    var category: String? = null
    var purchased = false
}
