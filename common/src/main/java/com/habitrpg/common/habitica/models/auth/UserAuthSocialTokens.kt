package com.habitrpg.common.habitica.models.auth

@Suppress("PropertyName")
data class UserAuthSocialTokens(
    var client_id: String? = null,
    var access_token: String? = null
)
