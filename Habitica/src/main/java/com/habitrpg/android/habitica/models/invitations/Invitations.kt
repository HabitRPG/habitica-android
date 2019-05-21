package com.habitrpg.android.habitica.models.invitations

import com.habitrpg.android.habitica.models.user.User

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Invitations : RealmObject() {

    @PrimaryKey
    var userId: String? = null

    internal var user: User? = null

    var party: PartyInvite? = null
    var parties: RealmList<PartyInvite>? = null
    private var guilds: RealmList<GuildInvite>? = null

    /**
     * @return The guilds invite
     */
    fun getGuilds(): List<GuildInvite>? {
        return guilds
    }

    /**
     * @param guilds The guilds
     */
    fun setGuilds(guilds: RealmList<GuildInvite>) {
        this.guilds = guilds
    }

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
