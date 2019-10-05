package com.habitrpg.shared.habitica.models.invitations

import com.habitrpg.shared.habitica.models.user.User
import com.habitrpg.shared.habitica.nativeLibraries.NativeRealmList

expect open class Invitations {

    var userId: String?

    internal var user: User?

    var party: PartyInvite?
    var parties: NativeRealmList<PartyInvite>?
    var guilds: NativeRealmList<GuildInvite>?

    fun removeInvitation(groupID: String)
}
