package com.habitrpg.shared.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


actual open class OwnedPet : RealmObject(), OwnedObject {

    @PrimaryKey
    actual override var combinedKey: String? = null
    actual override var userID: String? = null
        set(value) {
            field = value
            combinedKey = field + key
        }
    actual override var key: String? = null
        set(value) {
            field = value
            combinedKey = field + key
        }

    actual var trained: Int = 0

    actual override fun equals(other: Any?): Boolean {
        return if (other?.javaClass == OwnedPet::class.java) {
            this.combinedKey == (other as OwnedPet).combinedKey
        } else super.equals(other)
    }

    actual override fun hashCode(): Int {
        var result = combinedKey.hashCode()
        result = 31 * result + userID.hashCode()
        result = 31 * result + key.hashCode()
        return result
    }
}
