package com.habitrpg.wearos.habitica.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class Profile {
    var name: String? = null
}