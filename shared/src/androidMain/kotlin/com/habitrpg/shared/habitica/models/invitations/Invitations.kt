package com.habitrpg.shared.habitica.models.invitations

import com.habitrpg.shared.habitica.models.user.User
import io.realm.RealmList


import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class Invitations : RealmObject() {

    @PrimaryKey
    actual var userId: String? = null

    internal actual var user: User? = null

    actual var party: PartyInvite? = null
    actual var parties: RealmList<PartyInvite>? = null
    actual var guilds: RealmList<GuildInvite>? = null

    actual fun removeInvitation(groupID: String) {
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
