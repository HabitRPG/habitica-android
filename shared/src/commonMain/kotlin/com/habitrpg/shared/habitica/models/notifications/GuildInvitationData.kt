package com.habitrpg.shared.habitica.models.notifications

import kotlinx.serialization.Serializable

@Serializable
open class GuildInvitationData : NotificationData {
    var invitation: GuildInvite? = null
}

@Serializable
class GuildInvite {
    var id: String? = null
    var inviter: String? = null
    var name: String? = null
    var publicGuild: Boolean? = null
}
