package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativeLibraries.NativeDate

expect open class AuthenticationTimestamps {
    var userId: String?

    var lastLoggedIn: NativeDate?
    var createdAt: NativeDate?
}
