package com.habitrpg.android.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class OwnedCustomization : RealmObject(), OwnedObject {

    @PrimaryKey
    override var combinedKey: String? = null
    override var userID: String? = null
        set(value) {
            field = value
            combinedKey = field + type + category + key
        }
    override var key: String? = null
        set(value) {
            field = value
            combinedKey = userID + type + category +field
        }

    var type: String? = null
        set(value) {
            field = value
            combinedKey = userID + field + category +key
        }
    var category: String? = null
        set(value) {
            field = value
            combinedKey = userID + type + field + key
        }
    var purchased = false
}
