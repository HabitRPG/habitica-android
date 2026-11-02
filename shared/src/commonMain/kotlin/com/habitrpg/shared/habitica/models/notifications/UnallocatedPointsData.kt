package com.habitrpg.shared.habitica.models.notifications

import kotlinx.serialization.Serializable

@Serializable
open class UnallocatedPointsData : NotificationData {
    var points: Int? = null
}
