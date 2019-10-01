package com.habitrpg.shared.habitica.models.user

actual open class Inbox {

    /**
     * @return The optOut
     */
    /**
     * @return The blocks
     */
    /**
     * @return The newMessages
     */
    actual var userId: String?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    internal actual var user: User?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    /**
     * @param optOut The optOut
     */
    actual var optOut: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    /**
     * @param blocks The blocks
     */
    actual var blocks: List<Any>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    /**
     * @param newMessages The newMessages
     */
    actual var newMessages: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
}