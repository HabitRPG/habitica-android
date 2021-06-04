package com.habitrpg.android.habitica.models.user


import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class SpecialItems : RealmObject(), BaseObject {
    var ownedItems: RealmList<OwnedItem>? = null
    var seafoam: Int = 0
    var shinySeed: Int = 0
    var snowball: Int = 0
    var spookySparkles: Int = 0

    fun hasSpecialItems(): Boolean {
        return seafoam > 0 || shinySeed > 0 || snowball > 0 || spookySparkles > 0
    }
}
