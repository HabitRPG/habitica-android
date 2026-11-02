package com.habitrpg.android.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.RealmClass
import kotlinx.serialization.Serializable

@RealmClass(embedded = true)
@Serializable
open class OwnedCustomization :
    RealmObject(),
    OwnedObject {
    override var userID: String? = null
    override var key: String? = null

    var type: String? = null
    var category: String? = null
    var purchased = false
}
