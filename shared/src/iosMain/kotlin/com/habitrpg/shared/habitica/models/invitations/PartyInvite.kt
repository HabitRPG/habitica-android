package com.habitrpg.shared.habitica.models.invitations

import com.habitrpg.shared.habitica.models.members.PlayerTier
import com.habitrpg.shared.habitica.nativeLibraries.NativeContext

actual class PartyInvite {
    actual var id: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var name: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var inviter: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    actual companion object {
        actual fun getTiers(): List<PlayerTier> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        actual fun getColorForTier(context: NativeContext, value: Int): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

}