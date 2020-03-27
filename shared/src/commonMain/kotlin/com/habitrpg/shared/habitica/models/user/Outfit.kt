package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation
import com.habitrpg.shared.habitica.nativePackages.annotations.SerializedNameAnnotation

expect fun nativeIsAvailable(outfit: String): Boolean

open class Outfit : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var userId: String? = null

    internal var gear: Gear? = null
    var armor: String = ""
    var back: String = ""
    var body: String = ""
    var head: String = ""
    var shield: String = ""
    var weapon: String = ""
    @SerializedNameAnnotation("eyewear")
    var eyeWear: String = ""
    var headAccessory: String = ""

    fun isAvailable(outfit: String): Boolean = nativeIsAvailable(outfit)

    fun updateWith(newOutfit: Outfit) {
        this.armor = newOutfit.armor
        this.back = newOutfit.back
        this.body = newOutfit.body
        this.eyeWear = newOutfit.eyeWear
        this.head = newOutfit.head
        this.headAccessory = newOutfit.headAccessory
        this.shield = newOutfit.shield
        this.weapon = newOutfit.weapon
    }
}
