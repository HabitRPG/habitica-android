package com.habitrpg.common.habitica.models.notifications

open class PartyInvite {
    var id: String? = null
    var name: String? = null
    var inviter: String? = null
}

open class PartyInvitationData : NotificationData {
    var invitation: PartyInvite? = null
}
