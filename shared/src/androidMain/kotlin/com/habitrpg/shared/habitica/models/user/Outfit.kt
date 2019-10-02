package com.habitrpg.shared.habitica.models.user

import android.text.TextUtils

import com.google.gson.annotations.SerializedName

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class Outfit : RealmObject() {

    @PrimaryKey
    actual var userId: String? = null

    internal actual var gear: Gear? = null
    actual var armor: String = ""
    actual var back: String = ""
    actual var body: String = ""
    actual var head: String = ""
    actual var shield: String = ""
    actual var weapon: String = ""
    @SerializedName("eyewear")
    actual var eyeWear: String = ""
    actual var headAccessory: String = ""

    actual fun isAvailable(outfit: String): Boolean {
        return !TextUtils.isEmpty(outfit) && !outfit.endsWith("base_0")
    }

    actual fun updateWith(newOutfit: Outfit) {
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
