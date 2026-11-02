package com.habitrpg.shared.habitica.models.responses

import kotlinx.serialization.Serializable

@Serializable
class VerifyUsernameResponse {
    var isUsable: Boolean = false
    var issues = emptyList<String>()
}
