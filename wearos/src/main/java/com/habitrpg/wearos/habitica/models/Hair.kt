package com.habitrpg.wearos.habitica.models

import com.habitrpg.common.habitica.models.AvatarHair

class Hair: AvatarHair {
    override var mustache: Int = 0
    override var beard: Int = 0
    override var bangs: Int = 0
    override var base: Int = 0
    override var flower: Int = 0
    override var color: String? = null
}
