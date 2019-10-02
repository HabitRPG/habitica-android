package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.models.inventory.Equipment
import com.habitrpg.shared.habitica.nativeLibraries.NativeList

expect open class Gear {

    var userId: String?
    var owned: NativeList<Equipment>?
    internal var items: Items?
    var equipped: Outfit?
    var costume: Outfit?
}
