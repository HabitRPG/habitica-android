package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.models.inventory.Equipment
import com.habitrpg.shared.habitica.nativePackages.NativeList
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation


open class Gear : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var userId: String? = null
        set(userId) {
            field = userId
            if (costume != null && costume?.isManaged() == false) {
                costume!!.userId = userId
            }
            if (equipped != null && equipped?.isManaged() == false) {
                equipped!!.userId = userId + "equipped"
            }
        }

    var owned: NativeList<Equipment>? = null
    internal var items: Items? = null
    var equipped: Outfit? = null
    var costume: Outfit? = null
}
