package com.habitrpg.shared.habitica.models.invitations

import com.habitrpg.shared.habitica.models.user.User
import com.habitrpg.shared.habitica.nativeLibraries.RealmListWrapper

expect open class Invitations {

    var userId: String?

    internal var user: User?

    var party: PartyInvite?
    var parties: RealmListWrapper<PartyInvite>?
    var guilds: RealmListWrapper<GuildInvite>?

    fun removeInvitation(groupID: String)
}
