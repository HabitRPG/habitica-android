package com.habitrpg.android.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class OwnedMount : RealmObject(), OwnedObject {

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

    var owned = false

    override fun equals(other: Any?): Boolean {
        return if (other?.javaClass == OwnedMount::class.java) {
            this.combinedKey == (other as OwnedMount).combinedKey
        } else super.equals(other)
    }

    override fun hashCode(): Int {
        var result = combinedKey.hashCode()
        result = 31 * result + userID.hashCode()
        result = 31 * result + key.hashCode()
        return result
    }
}
