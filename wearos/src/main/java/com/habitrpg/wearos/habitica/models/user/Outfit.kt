package com.habitrpg.wearos.habitica.models.user

import com.habitrpg.shared.habitica.models.AvatarOutfit
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Outfit : AvatarOutfit {
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
