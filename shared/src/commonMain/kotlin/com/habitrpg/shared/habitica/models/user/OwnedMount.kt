package com.habitrpg.shared.habitica.models.user

expect open class OwnedMount : OwnedObject {

    override var combinedKey: String?
    override var userID: String?
    override var key: String?

    var owned: Boolean

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int
}
