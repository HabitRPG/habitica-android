package com.habitrpg.shared.habitica.models.notifications

import kotlinx.serialization.Serializable

@Serializable
open class PartyInvite {
    var id: String? = null
    var name: String? = null
    var inviter: String? = null
}

@Serializable
open class PartyInvitationData : NotificationData {
    var invitation: PartyInvite? = null
}
