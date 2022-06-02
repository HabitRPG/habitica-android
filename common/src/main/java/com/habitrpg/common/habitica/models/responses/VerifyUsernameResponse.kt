package com.habitrpg.common.habitica.models.responses

class VerifyUsernameResponse {

    var isUsable: Boolean = false
    var issues = emptyList<String>()
}
