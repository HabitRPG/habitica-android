package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import kotlinx.serialization.Serializable

@RealmClass(embedded = true)
@Serializable
open class ABTest :
    RealmObject(),
    BaseObject {
    var name: String = ""
    var group: String = ""
}
