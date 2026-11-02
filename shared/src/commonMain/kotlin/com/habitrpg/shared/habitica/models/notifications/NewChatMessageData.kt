package com.habitrpg.shared.habitica.models.notifications

import kotlinx.serialization.Serializable

@Serializable
open class NewChatMessageData : NotificationData {
    var group: NotificationGroup? = null
}
