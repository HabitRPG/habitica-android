package com.habitrpg.android.habitica.models.invitations

import com.habitrpg.android.habitica.utils.InviteResponseSerializer
import kotlinx.serialization.Serializable

@Serializable(with = InviteResponseSerializer::class)
sealed class InviteResponse {
    data class UserInvite(
        val id: String,
        val name: String,
        val inviter: String,
    ) : InviteResponse()

    data class EmailInvite(
        val email: String,
    ) : InviteResponse()
}
