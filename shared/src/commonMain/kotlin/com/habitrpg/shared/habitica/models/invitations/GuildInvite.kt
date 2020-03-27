package com.habitrpg.shared.habitica.models.invitations

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class GuildInvite : NativeRealmObject(), GenericInvitation {

    @PrimaryKeyAnnotation
    override var id: String? = null
    override var inviter: String? = null
    override var name: String? = null
    var publicGuild: Boolean? = null
}
