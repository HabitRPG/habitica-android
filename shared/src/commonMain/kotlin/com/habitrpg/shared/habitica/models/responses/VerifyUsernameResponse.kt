package com.habitrpg.shared.habitica.models.responses

class VerifyUsernameResponse {

    var isUsable: Boolean = false
    var issues = emptyList<String>()
}
