package com.habitrpg.shared.habitica.models.notifications

import kotlinx.serialization.Serializable

@Serializable
open class GroupTaskApprovedData : NotificationData {
    var groupId: String = ""
    var message: String? = null
}
