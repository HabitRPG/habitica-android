package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class ABTest : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var userID: String? = null

    var name: String = ""
    var group: String = ""
}