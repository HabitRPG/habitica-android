package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.shared.habitica.models.AvatarOutfit
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@RealmClass(embedded = true)
@Serializable
open class Outfit :
    RealmObject(),
    BaseObject,
    AvatarOutfit {
    override var armor: String = ""
    override var back: String = ""
    override var body: String = ""
    override var head: String = ""
    override var shield: String = ""
    override var weapon: String = ""

    @SerialName("eyewear")
    override var eyeWear: String = ""
    override var headAccessory: String = ""
}
