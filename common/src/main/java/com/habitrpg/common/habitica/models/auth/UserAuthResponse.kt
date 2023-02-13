package com.habitrpg.common.habitica.models.auth

class UserAuthResponse {
    // we need apiToken and token, as both are possible returns
    var apiToken: String = ""
    var token: String
        get() {
            return apiToken
        }
        set(value) {
            apiToken = value
        }
    var newUser = false
    var id: String = ""
}
