package com.habitrpg.shared.habitica.models.notifications

import kotlinx.serialization.Serializable

@Serializable
open class QuestInvitationData : NotificationData {
    var questKey: String? = null
}
