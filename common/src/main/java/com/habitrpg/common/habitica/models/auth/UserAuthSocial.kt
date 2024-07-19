package com.habitrpg.common.habitica.models.auth

data class UserAuthSocial (
    val network: String? = null,
    val authResponse: UserAuthSocialTokens? = null
)
