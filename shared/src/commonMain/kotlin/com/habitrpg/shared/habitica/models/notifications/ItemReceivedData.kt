package com.habitrpg.shared.habitica.models.notifications

import kotlinx.serialization.Serializable

@Serializable
open class ItemReceivedData : NotificationData {
    var title: String? = null
    var text: String? = null
    var icon: String? = null
    var destination: String? = null
}
