package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class OwnedCustomization : NativeRealmObject(), OwnedObject {

    @PrimaryKeyAnnotation
    override var combinedKey: String? = null
    override var userID: String? = null
        set(value) {
            field = value
            combinedKey = field + type + key
        }
    override var key: String? = null
        set(value) {
            field = value
            combinedKey = userID + type + field
        }

    var type: String? = null
        set(value) {
            field = value
            combinedKey = userID + field + key
        }
    var category: String? = null
    var purchased = false
}
