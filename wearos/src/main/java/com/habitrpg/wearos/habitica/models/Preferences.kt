package com.habitrpg.wearos.habitica.models

import com.habitrpg.common.habitica.models.AvatarHair
import com.habitrpg.common.habitica.models.AvatarPreferences

class Preferences: AvatarPreferences {
    override val hair: Hair? = null
    override val costume: Boolean = false
    override val sleep: Boolean = false
    override val shirt: String? = null
    override val skin: String? = null
    override val size: String? = null
    override val background: String? = null
    override val chair: String? = null
    override val disableClasses: Boolean = false
}
