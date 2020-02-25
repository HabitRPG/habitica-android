package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.IgnoreAnnotation
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class Inbox : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var userId: String? = null

    internal var user: User? = null

    /**
     * @return The optOut
     * @value optOut The optOut
     */
    var optOut: Boolean = false

    /**
     * @return The blocks
     * @value blocks The blocks
     */
    @IgnoreAnnotation
    var blocks: List<Any> = ArrayList()

    /**
     * @return The newMessages
     * @value newMessages The newMessages
     */
    var newMessages: Int = 0
}