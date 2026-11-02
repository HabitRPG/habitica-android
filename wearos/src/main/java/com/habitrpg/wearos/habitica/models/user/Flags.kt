package com.habitrpg.wearos.habitica.models.user

import com.habitrpg.shared.habitica.models.AvatarFlags
import kotlinx.serialization.Serializable

@Serializable
class Flags : AvatarFlags {
    override var classSelected: Boolean = false
}
