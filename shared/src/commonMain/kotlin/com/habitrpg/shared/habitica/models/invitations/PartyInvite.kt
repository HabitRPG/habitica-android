package com.habitrpg.shared.habitica.models.invitations

import com.habitrpg.shared.habitica.models.members.PlayerTier
import com.habitrpg.shared.habitica.nativeLibraries.NativeContext

expect class PartyInvite {

    var id: String?
    var name: String?

    var inviter: String?

    companion object {
        fun getTiers(): List<PlayerTier>
        fun getColorForTier(context: NativeContext, value: Int): Int
    }
}
