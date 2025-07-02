package com.habitrpg.android.habitica.models.invitations

sealed class InviteResponse {
    data class UserInvite(
        val id: String,
        val name: String,
        val inviter: String
    ) : InviteResponse()

    data class EmailInvite(
        val email: String
    ) : InviteResponse()
}
