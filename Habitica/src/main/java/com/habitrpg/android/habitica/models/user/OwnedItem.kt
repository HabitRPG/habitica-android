package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseMainObject
import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class OwnedItem : RealmObject(), BaseMainObject, OwnedObject {
    override val realmClass: Class<OwnedItem>
        get() = OwnedItem::class.java
    override val primaryIdentifier: String?
        get() = key
    override val primaryIdentifierName: String
        get() = "combinedKey"

    override var userID: String? = null

    override var key: String? = null

    var itemType: String? = null
    var numberOwned = 0
}
