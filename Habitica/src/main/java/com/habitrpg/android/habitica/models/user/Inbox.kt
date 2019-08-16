package com.habitrpg.android.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import java.util.*

open class Inbox : RealmObject() {

    @PrimaryKey
    var userId: String? = null

    internal var user: User? = null
    /**
     * @return The optOut
     */
    /**
     * @param optOut The optOut
     */
    var optOut: Boolean = false
    /**
     * @return The blocks
     */
    /**
     * @param blocks The blocks
     */
    @Ignore
    var blocks: List<Any> = ArrayList()
    /**
     * @return The newMessages
     */
    /**
     * @param newMessages The newMessages
     */
    var newMessages: Int = 0
}