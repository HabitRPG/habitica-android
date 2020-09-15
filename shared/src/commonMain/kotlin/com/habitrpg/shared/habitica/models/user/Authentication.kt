package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.models.auth.LocalAuthentication
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation
import com.habitrpg.shared.habitica.nativePackages.annotations.SerializedNameAnnotation

open class Authentication : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var userId: String? = null
        set(value) {
            field = value
            timestamps?.userId = value
            localAuthentication?.userID = value
        }

    @SerializedNameAnnotation("local")
    var localAuthentication: LocalAuthentication? = null

    var hasFacebookAuth = false
    var hasGoogleAuth = false
    var hasAppleAuth = false

    var timestamps: AuthenticationTimestamps? = null

}
