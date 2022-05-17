package com.habitrpg.common.habitica.models

import android.text.TextUtils

interface AvatarOutfit {
    var armor: String
    var back: String
    var body: String
    var head: String
    var shield: String
    var weapon: String
    var eyeWear: String
    var headAccessory: String


    fun isAvailable(outfit: String): Boolean {
        return !TextUtils.isEmpty(outfit) && !outfit.endsWith("base_0")
    }

    fun updateWith(newOutfit: AvatarOutfit) {
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
