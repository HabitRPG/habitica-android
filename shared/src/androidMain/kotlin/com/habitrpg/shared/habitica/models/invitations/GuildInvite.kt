package com.habitrpg.shared.habitica.models.invitations

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class GuildInvite : RealmObject() {

    @PrimaryKey
    actual var id: String? = null

    internal actual var invitations: Invitations? = null

    actual var inviter: String? = null

    actual var name: String? = null

    actual var publicGuild: Boolean? = null
}
