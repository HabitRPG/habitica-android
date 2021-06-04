package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.models.inventory.Equipment
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class Gear : RealmObject(), BaseObject {
    var owned: RealmList<Equipment>? = null
    var equipped: Outfit? = null
    var costume: Outfit? = null
}