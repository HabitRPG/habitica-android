package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativeLibraries.RealmListWrapper


expect open class SpecialItems {

    var userId: String?
    var ownedItems: RealmListWrapper<OwnedItem>?
    var seafoam: Int
    var shinySeed: Int
    var snowball: Int
    var spookySparkles: Int

    fun hasSpecialItems(): Boolean
}
