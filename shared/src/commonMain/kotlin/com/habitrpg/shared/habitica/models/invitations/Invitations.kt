package com.habitrpg.shared.habitica.models.invitations

import com.habitrpg.shared.habitica.models.user.User
import com.habitrpg.shared.habitica.nativePackages.NativeList
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class Invitations : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var userId: String? = null

    internal var user: User? = null

    var party: PartyInvite? = null
    var parties: NativeList<PartyInvite>? = null
    var guilds: NativeList<GuildInvite>? = null

    fun removeInvitation(groupID: String) {
        if (party?.id == groupID) {
            party = null
        }

        guilds?.removeAll {
            it.id == groupID
        }

        parties?.removeAll {
            it.id == groupID
        }
    }
}
