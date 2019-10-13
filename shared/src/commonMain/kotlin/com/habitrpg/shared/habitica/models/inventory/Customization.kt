package com.habitrpg.shared.habitica.models.inventory

import com.habitrpg.shared.habitica.nativeLibraries.NativeDate

expect open class Customization {

    var id: String?
    var identifier: String?
    var category: String?
    var type: String?
    var notes: String?
    var customizationSet: String
    var customizationSetName: String?
    var text: String?
    var purchased: Boolean
    var isBuyable: Boolean
    var price: Int
    var setPrice: Int?
    var availableFrom: NativeDate?
    var availableUntil: NativeDate?

    //Not released yet
    //Discontinued
    val purchasable: Boolean

    val isUsable: Boolean

    val path: String

    fun updateID()

    fun getImageName(userSize: String?, hairColor: String?): String
}
