package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.models.auth.LocalAuthentication

actual open class Authentication {
    actual var userId: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var localAuthentication: LocalAuthentication?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var timestamps: AuthenticationTimestamps?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

}