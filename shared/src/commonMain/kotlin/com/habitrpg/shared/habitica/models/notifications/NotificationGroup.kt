package com.habitrpg.shared.habitica.models.notifications

import kotlinx.serialization.Serializable

@Serializable
open class NotificationGroup {
    var id: String = ""

    var name: String? = null
}
