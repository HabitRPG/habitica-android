package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation


open class OwnedItem : NativeRealmObject(), OwnedObject {

    @PrimaryKeyAnnotation
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
