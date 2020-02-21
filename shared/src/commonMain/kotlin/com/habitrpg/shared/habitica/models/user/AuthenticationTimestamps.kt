package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativePackages.NativeDate
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation
import com.habitrpg.shared.habitica.nativePackages.annotations.SerializedNameAnnotation

open class AuthenticationTimestamps : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var userId: String? = null

    @SerializedNameAnnotation("loggedin")
    var lastLoggedIn: NativeDate? = null
    @SerializedNameAnnotation("created")
    var createdAt: NativeDate? = null
}
