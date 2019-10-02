package com.habitrpg.shared.habitica.models.user

expect open class OwnedPet : OwnedObject {

    override var combinedKey: String?
    override var userID: String?
    override var key: String?

    var trained: Int

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int
}
