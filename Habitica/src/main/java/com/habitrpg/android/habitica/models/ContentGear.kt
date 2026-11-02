@file:UseSerializers(RealmListSerializer::class)

package com.habitrpg.android.habitica.models

import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.utils.EquipmentListSerializer
import com.habitrpg.android.habitica.utils.RealmListSerializer
import io.realm.RealmList
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
class ContentGear {
    @Serializable(with = EquipmentListSerializer::class)
    var flat: RealmList<Equipment>? = null
}
