package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import java.util.ArrayList

@RealmClass(embedded = true)
open class SpecialItems : RealmObject(), BaseObject {
    var ownedItems: RealmList<OwnedItem>? = null
    var seafoam: Int = 0
    var shinySeed: Int = 0
    var snowball: Int = 0
    var spookySparkles: Int = 0

    val hasSpecialItems: Boolean
        get() {
            return seafoam > 0 || shinySeed > 0 || snowball > 0 || spookySparkles > 0
        }

    fun getSpecialItemCount(key: String): Int{
        var inventoryPresent: OwnedItem = ownedItems?.filter { it.key == "inventory_present" }?.single() ?: OwnedItem()

        var count = 0
        when(key){
            "seafoam" -> count = seafoam
            "shinySeed" -> count = shinySeed
            "snowball" -> count = snowball
            "spookySparkles" -> count = spookySparkles
            "inventory_present" -> count = inventoryPresent.numberOwned
        }
        return count
    }

    fun getOwnedItemsToTypedArray(): Array<String> {
        val ownedSpecialItems = ArrayList<String>()
        ownedSpecialItems.add("inventory_present")
        if (snowball > 0) {
            ownedSpecialItems.add("snowball")
        }
        if (shinySeed > 0) {
            ownedSpecialItems.add("shinySeed")
        }
        if (seafoam > 0) {
            ownedSpecialItems.add("seafoam")
        }
        if (spookySparkles > 0) {
            ownedSpecialItems.add("spookySparkles")
        }

        if (ownedSpecialItems.size == 0) {
            ownedSpecialItems.add("")
        }

        return ownedSpecialItems.toTypedArray()
    }
}
