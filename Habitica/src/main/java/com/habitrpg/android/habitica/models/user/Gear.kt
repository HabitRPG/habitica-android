package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.shared.habitica.models.AvatarGear
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class Gear : RealmObject(), BaseObject, AvatarGear {
    var owned: RealmList<Equipment>? = null
    override var equipped: Outfit? = null
    override var costume: Outfit? = null
}
