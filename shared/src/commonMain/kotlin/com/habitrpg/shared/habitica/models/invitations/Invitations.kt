package com.habitrpg.shared.habitica.models.invitations

import com.habitrpg.shared.habitica.models.user.User
import com.habitrpg.shared.habitica.nativeLibraries.NativeList

expect open class Invitations {

    var userId: String?

    internal var user: User?

    var party: PartyInvite?
    var parties: NativeList<PartyInvite>?
    var guilds: NativeList<GuildInvite>?

    fun removeInvitation(groupID: String)
}
