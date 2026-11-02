package com.habitrpg.shared.habitica.models.responses

import kotlinx.serialization.Serializable

@Serializable
class VerifyEmailResponse {
    var email: String? = null
    var valid: Boolean = false
    var error: String? = null
}
