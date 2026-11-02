package com.habitrpg.shared.habitica.models.auth

import kotlinx.serialization.Serializable

@Suppress("PropertyName")
@Serializable
class UserAuthSocialTokens {
    var client_id: String? = null
    var access_token: String? = null
}
