package com.habitrpg.shared.habitica.models.user

expect open class SuppressedModals {
    var userId: String?

    internal var preferences: Preferences?
    var streak: Boolean?
    var raisePet: Boolean?
    var hatchPet: Boolean?
    var levelUp: Boolean?
}
