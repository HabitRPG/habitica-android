package com.habitrpg.shared.habitica.models.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserAuthResponse(
    val apiToken: String = "",
    val id: String = "",
    val newUser: Boolean = false,
    var userExists: Boolean = true,
) {
    val token: String
        get() = apiToken
}
