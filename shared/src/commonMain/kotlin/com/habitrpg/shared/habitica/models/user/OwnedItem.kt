package com.habitrpg.shared.habitica.models.user

expect open class OwnedItem : OwnedObject {
    override var combinedKey: String?
    override var userID: String?
    override var key: String?

    var itemType: String?
    var numberOwned: Int
}
