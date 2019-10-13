package com.habitrpg.shared.habitica.models.invitations

import com.habitrpg.shared.habitica.models.user.User
import com.habitrpg.shared.habitica.nativeLibraries.RealmList

actual open class Invitations {
    actual var userId: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    internal actual var user: User?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var party: PartyInvite?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var parties: RealmList<PartyInvite>?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var guilds: RealmList<GuildInvite>?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    actual fun removeInvitation(groupID: String) {
    }

}