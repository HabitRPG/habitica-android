package com.habitrpg.common.habitica.models.notifications

open class GuildInvitationData : NotificationData {

    var invitation: GuildInvite? = null
}

class GuildInvite {
    var id: String? = null
    var inviter: String? = null
    var name: String? = null
    var publicGuild: Boolean? = null
}
