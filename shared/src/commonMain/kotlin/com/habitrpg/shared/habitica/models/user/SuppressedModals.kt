package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class SuppressedModals : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var userId: String? = null

    internal var preferences: Preferences? = null
    var streak: Boolean? = null
    var raisePet: Boolean? = null
    var hatchPet: Boolean? = null
    var levelUp: Boolean? = null
}
