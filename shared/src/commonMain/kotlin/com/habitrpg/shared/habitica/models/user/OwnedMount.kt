package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation


open class OwnedMount : NativeRealmObject(), OwnedObject {

    @PrimaryKeyAnnotation
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
        return if (other is OwnedMount) {
            this.combinedKey == other.combinedKey
        } else super.equals(other)
    }

    override fun hashCode(): Int {
        var result = combinedKey.hashCode()
        result = 31 * result + userID.hashCode()
        result = 31 * result + key.hashCode()
        return result
    }
}
