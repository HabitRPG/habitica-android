package com.habitrpg.shared.habitica.models.user

import android.text.TextUtils

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation
import com.habitrpg.shared.habitica.nativePackages.annotations.SerializedNameAnnotation

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

    fun isAvailable(outfit: String): Boolean {
        return !TextUtils.isEmpty(outfit) && !outfit.endsWith("base_0")
    }

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
