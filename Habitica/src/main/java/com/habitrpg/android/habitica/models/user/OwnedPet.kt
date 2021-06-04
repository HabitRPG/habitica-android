package com.habitrpg.android.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass


@RealmClass(embedded = true)
open class OwnedPet : RealmObject(), OwnedObject {
    override var userID: String? = null
    override var key: String? = null
    var trained = 0

    override fun equals(other: Any?): Boolean {
        return if (other is OwnedPet) {
            userID == other.userID && key == other.key
        } else super.equals(other)
    }

    override fun hashCode(): Int {
        var result = userID?.hashCode() ?: 0
        result = 31 * result + (key?.hashCode() ?: 0)
        return result
    }
}
