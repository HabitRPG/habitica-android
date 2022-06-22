package com.habitrpg.wearos.habitica.models.user

import com.habitrpg.common.habitica.models.AvatarFlags
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class Flags: AvatarFlags {
    override var classSelected: Boolean = false
}