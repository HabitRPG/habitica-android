@file:UseSerializers(DateSerializer::class)

package com.habitrpg.android.habitica.models.inventory

import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.shared.habitica.serialization.DateSerializer
import io.realm.RealmModel
import io.realm.RealmObject
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.Date

@Serializable
open class ItemEvent :
    RealmObject(),
    BaseObject {
    var start: Date? = null
    var end: Date? = null
}

interface Item : RealmModel {
    val type: String
    val key: String
    val text: String
    val value: Int
    var event: ItemEvent?
}
