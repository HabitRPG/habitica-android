package com.habitrpg.wearos.habitica.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class Items {
    var gear: Gear? = null

    var currentMount: String? = null
    var currentPet: String? = null
}