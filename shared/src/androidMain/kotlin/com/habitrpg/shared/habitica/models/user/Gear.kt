package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.models.inventory.Equipment
import com.habitrpg.shared.habitica.nativeLibraries.RealmListWrapper


import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class Gear : RealmObject() {

    @PrimaryKey
    actual var userId: String? = null
        set(userId) {
            field = userId
            if (costume != null && !costume!!.isManaged) {
                costume!!.userId = userId
            }
            if (equipped != null && !equipped!!.isManaged) {
                equipped!!.userId = userId + "equipped"
            }
        }

    actual var owned: RealmListWrapper<Equipment>? = null
    internal actual var items: Items? = null
    actual var equipped: Outfit? = null
    actual var costume: Outfit? = null
}
