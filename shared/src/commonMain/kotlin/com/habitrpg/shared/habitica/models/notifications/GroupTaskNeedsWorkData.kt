package com.habitrpg.shared.habitica.models.notifications

import kotlinx.serialization.Serializable

@Serializable
open class GroupTaskNeedsWorkData : NotificationData {
    var group: NotificationGroup? = null

    var message: String? = null
}
