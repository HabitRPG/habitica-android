package com.habitrpg.shared.habitica.models.auth

import kotlinx.serialization.Serializable

@Serializable
class UserAuthSocial {
    var network: String? = null
    var authResponse: UserAuthSocialTokens? = null
    var allowRegister: Boolean = true
}
