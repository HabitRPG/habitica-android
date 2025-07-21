package com.habitrpg.common.habitica.models.auth

data class UserAuthResponse(
    val apiToken: String = "",
    val id:       String = "",
    val newUser:  Boolean = false,
    var userExists: Boolean = false,
) {

    val token: String
        get() = apiToken
}
