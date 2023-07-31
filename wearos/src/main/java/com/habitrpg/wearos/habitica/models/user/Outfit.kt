package com.habitrpg.wearos.habitica.models.user

import com.habitrpg.shared.habitica.models.AvatarOutfit
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class Outfit : AvatarOutfit {
    override var armor: String = ""
    override var back: String = ""
    override var body: String = ""
    override var head: String = ""
    override var shield: String = ""
    override var weapon: String = ""

    @Json(name = "eyewear")
    override var eyeWear: String = ""
    override var headAccessory: String = ""
}
