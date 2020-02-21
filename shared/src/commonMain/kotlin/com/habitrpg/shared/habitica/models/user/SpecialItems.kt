package com.habitrpg.shared.habitica.models.user


import com.habitrpg.shared.habitica.nativePackages.NativeList
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class SpecialItems : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var userId: String? = null
        set(userId) {
            field = userId
            ownedItems?.forEach {
                if (!it.isManaged()) {
                    it.userID = userId
                    it.itemType = "eggs"
                }
            }
        }
    var ownedItems: NativeList<OwnedItem>? = null
    var seafoam: Int = 0
    var shinySeed: Int = 0
    var snowball: Int = 0
    var spookySparkles: Int = 0

    fun hasSpecialItems(): Boolean {
        return seafoam > 0 || shinySeed > 0 || snowball > 0 || spookySparkles > 0
    }
}
