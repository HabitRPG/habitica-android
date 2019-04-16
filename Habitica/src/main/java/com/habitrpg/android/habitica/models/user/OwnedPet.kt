package com.habitrpg.android.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class OwnedPet : RealmObject(), OwnedObject {

    @PrimaryKey
    override var combinedKey: String? = null
    override var userID: String? = null
        set(value) {
            field = value
            combinedKey = field + key
        }
    override var key: String? = null
        set(value) {
            field = value
            combinedKey = field + key
        }

    var trained = 0
}
