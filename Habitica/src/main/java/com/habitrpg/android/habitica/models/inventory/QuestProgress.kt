@file:UseSerializers(RealmListSerializer::class)

package com.habitrpg.android.habitica.models.inventory

import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.utils.RealmListSerializer
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@RealmClass(embedded = true)
@Serializable
open class QuestProgress :
    RealmObject(),
    BaseObject {
    var id: String? = null
    var key: String? = null
    var hp: Double = 0.0
    var rage: Double = 0.0
    var collectedItems: Int = 0
    var collect: RealmList<QuestProgressCollect>? = RealmList()
    var down: Float = 0.0f
    var up: Float = 0.0f
}
