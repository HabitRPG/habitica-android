package com.habitrpg.shared.habitica.models.auth

import kotlinx.serialization.Serializable

@Serializable
class UserAuth(
    var username: String? = null,
    var password: String? = null,
    var confirmPassword: String? = null,
    var email: String? = null,
)
