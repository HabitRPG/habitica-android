package com.habitrpg.wearos.habitica.models.user

import com.habitrpg.shared.habitica.models.AvatarItems
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class Items : AvatarItems {
    override var gear: Gear? = null

    override var currentMount: String? = null
    override var currentPet: String? = null
}
