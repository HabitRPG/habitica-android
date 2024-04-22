package com.habitrpg.android.habitica.models.invitations

import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class GuildInvite : RealmObject(), GenericInvitation {
    override var id: String? = null
    override var inviter: String? = null
    override var name: String? = null
    var publicGuild: Boolean? = null
}
