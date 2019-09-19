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

    override fun equals(other: Any?): Boolean {
        return if (other?.javaClass == OwnedPet::class.java) {
            this.combinedKey == (other as OwnedPet).combinedKey
        } else super.equals(other)
    }

    override fun hashCode(): Int {
        var result = combinedKey.hashCode()
        result = 31 * result + userID.hashCode()
        result = 31 * result + key.hashCode()
        return result
    }
}
