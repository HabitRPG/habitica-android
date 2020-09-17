package com.habitrpg.android.habitica.models.inventory

class CustomizationSet {
    var text: String? = null
    var identifier: String? = null
    var price: Int = 0
    var hasPurchasable = false
    var customizations: MutableList<Customization> = mutableListOf()
}