package com.habitrpg.shared.habitica.models.user


import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class SpecialItems : RealmObject() {

    @PrimaryKey
    actual var userId: String? = null
        set(userId) {
            field = userId
            ownedItems?.forEach {
                if (!it.isManaged) {
                    it.userID = userId
                    it.itemType = "eggs"
                }
            }
        }
    actual var ownedItems: RealmList<OwnedItem>? = null
    actual var seafoam: Int = 0
    actual var shinySeed: Int = 0
    actual var snowball: Int = 0
    actual var spookySparkles: Int = 0

    actual fun hasSpecialItems(): Boolean {
        return seafoam > 0 || shinySeed > 0 || snowball > 0 || spookySparkles > 0
    }
}
