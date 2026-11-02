package com.habitrpg.android.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.RealmClass
import kotlinx.serialization.Serializable

@RealmClass(embedded = true)
@Serializable
open class OwnedEquipment :
    RealmObject(),
    OwnedObject {
    override var userID: String? = null
    override var key: String? = null
    var owned: Boolean? = null
}
