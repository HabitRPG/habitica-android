package com.habitrpg.android.habitica.models.inventory

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import kotlinx.serialization.Serializable

@RealmClass(embedded = true)
@Serializable
open class QuestProgressCollect :
    RealmObject(),
    BaseObject {
    var key: String? = null
    var count = 0
}
