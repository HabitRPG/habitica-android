package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativePackages.NativeList
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class Inbox : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var userId: String? = null

    internal var user: User? = null
    var optOut: Boolean = false
    var blocks: NativeList<String> = NativeList()
    var newMessages: Int = 0
}
