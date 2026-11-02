package com.habitrpg.shared.habitica.models.notifications

import kotlinx.serialization.Serializable

@Serializable
class GroupTaskRequiresApprovalData : NotificationData {
    var groupId: String = ""
    var message: String? = null
}
