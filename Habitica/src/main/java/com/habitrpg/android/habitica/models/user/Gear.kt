@file:UseSerializers(RealmListSerializer::class)

package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.utils.OwnedEquipmentListSerializer
import com.habitrpg.android.habitica.utils.RealmListSerializer
import com.habitrpg.shared.habitica.models.AvatarGear
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@RealmClass(embedded = true)
@Serializable
open class Gear :
    RealmObject(),
    BaseObject,
    AvatarGear {
    @Serializable(with = OwnedEquipmentListSerializer::class)
    var owned: RealmList<OwnedEquipment>? = RealmList()
    override var equipped: Outfit? = null
    override var costume: Outfit? = null
}
