package com.habitrpg.wearos.habitica.models.user

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class Gear {
    var equipped: Outfit? = null
    var costume: Outfit? = null
}