@file:UseSerializers(RealmListSerializer::class)

package com.habitrpg.android.habitica.models.inventory

import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.utils.QuestDropItemListSerializer
import com.habitrpg.android.habitica.utils.RealmListSerializer
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

/**
 * Created by phillip on 25.07.17.
 */
@Serializable
open class QuestDrops :
    RealmObject(),
    BaseObject {
    @PrimaryKey
    var key: String? = null
        set(value) {
            field = value
            items?.forEach { it.questKey = key }
        }
    var gp = 0
    var exp = 0
    var unlock: String? = null

    @Serializable(with = QuestDropItemListSerializer::class)
    var items: RealmList<QuestDropItem>? = null
        set(value) {
            field = value
            items?.forEach { it.questKey = key }
        }
}
