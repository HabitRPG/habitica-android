package com.habitrpg.shared.habitica.models.user

expect open class Outfit {

    var userId: String?

    internal var gear: Gear?
    var armor: String
    var back: String
    var body: String
    var head: String
    var shield: String
    var weapon: String
    var eyeWear: String
    var headAccessory: String

    fun isAvailable(outfit: String): Boolean

    fun updateWith(newOutfit: Outfit)
}
