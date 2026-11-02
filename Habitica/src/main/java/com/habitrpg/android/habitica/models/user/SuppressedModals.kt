package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import kotlinx.serialization.Serializable

@RealmClass(embedded = true)
@Serializable
open class SuppressedModals :
    RealmObject(),
    BaseObject {
    var streak: Boolean? = null
    var raisePet: Boolean? = null
    var hatchPet: Boolean? = null
    var levelUp: Boolean? = null
}
