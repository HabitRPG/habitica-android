package com.habitrpg.shared.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import java.util.*

actual open class Inbox : RealmObject() {

    @PrimaryKey
    actual var userId: String? = null

    internal actual var user: User? = null
    /**
     * @return The optOut
     */
    /**
     * @param optOut The optOut
     */
    actual var optOut: Boolean = false
    /**
     * @return The blocks
     */
    /**
     * @param blocks The blocks
     */
    @Ignore
    actual var blocks: List<Any> = ArrayList()
    /**
     * @return The newMessages
     */
    /**
     * @param newMessages The newMessages
     */
    actual var newMessages: Int = 0
}