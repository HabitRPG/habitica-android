package com.habitrpg.android.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class OwnedMount : RealmObject(), OwnedObject {
    override var userID: String? = null
    override var key: String? = null

    var owned = false
}
