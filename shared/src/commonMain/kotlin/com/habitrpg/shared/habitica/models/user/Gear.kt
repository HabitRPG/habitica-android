package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.models.inventory.Equipment
import com.habitrpg.shared.habitica.nativeLibraries.NativeRealmList

expect open class Gear {

    var userId: String?
    var owned: NativeRealmList<Equipment>?
    internal var items: Items?
    var equipped: Outfit?
    var costume: Outfit?
}
