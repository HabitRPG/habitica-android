package com.habitrpg.shared.habitica.models.notifications

import kotlinx.serialization.Serializable

@Serializable
open class FirstDropData : NotificationData {
    var egg: String? = null
    var hatchingPotion: String? = null
}
