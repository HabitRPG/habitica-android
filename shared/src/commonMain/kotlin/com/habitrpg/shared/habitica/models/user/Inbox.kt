package com.habitrpg.shared.habitica.models.user

expect open class Inbox {

    var userId: String?

    internal var user: User?
    /**
     * @return The optOut
     */
    /**
     * @param optOut The optOut
     */
    var optOut: Boolean
    /**
     * @return The blocks
     */
    /**
     * @param blocks The blocks
     */
    var blocks: List<Any>
    /**
     * @return The newMessages
     */
    /**
     * @param newMessages The newMessages
     */
    var newMessages: Int
}