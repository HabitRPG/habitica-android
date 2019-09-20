package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.models.auth.LocalAuthentication

expect open class Authentication {

    var userId: String?

    var localAuthentication: LocalAuthentication?

    var timestamps: AuthenticationTimestamps?
}
