package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseMainObject
import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
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

    override fun equals(other: Any?): Boolean {
        return if (other is OwnedItem) {
            userID == other.userID && key == other.key && itemType == other.itemType
        } else super.equals(other)
    }

    override fun hashCode(): Int {
        var result = userID?.hashCode() ?: 0
        result = 31 * result + (key?.hashCode() ?: 0)
        result = 31 * result + (itemType?.hashCode() ?: 0)
        return result
    }
}
