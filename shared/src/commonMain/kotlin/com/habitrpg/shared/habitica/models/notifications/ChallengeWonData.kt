package com.habitrpg.shared.habitica.models.notifications

import kotlinx.serialization.Serializable

@Serializable
open class ChallengeWonData : NotificationData {
    var name: String? = null
    var prize: Int? = null
}
