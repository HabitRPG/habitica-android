package com.habitrpg.android.habitica.models.user

import android.text.TextUtils

import com.google.gson.annotations.SerializedName

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Outfit : RealmObject() {

    @PrimaryKey
    var userId: String? = null

    internal var gear: Gear? = null
    var armor: String = ""
    var back: String = ""
    var body: String = ""
    var head: String = ""
    var shield: String = ""
    var weapon: String = ""
    @SerializedName("eyewear")
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
