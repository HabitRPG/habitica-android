package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class OwnedItem : RealmObject(), BaseObject, OwnedObject {

    override val realmClass: Class<OwnedItem>
        get() = OwnedItem::class.java
    override val primaryIdentifier: String?
        get() = combinedKey
    override val primaryIdentifierName: String
        get() = "combinedKey"

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
