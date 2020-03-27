package com.habitrpg.shared.habitica.models.auth


import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

/**
 * Created by admin on 18/11/15.
 */
open class LocalAuthentication : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var userID: String? = null
    var username: String? = null
    var email: String? = null
}
