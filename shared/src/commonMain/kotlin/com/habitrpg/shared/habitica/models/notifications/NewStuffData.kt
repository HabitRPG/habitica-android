package com.habitrpg.shared.habitica.models.notifications

import kotlinx.serialization.Serializable

@Serializable
open class NewStuffData : NotificationData {
    var title: String? = null
}
