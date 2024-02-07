package com.habitrpg.android.habitica.models.inventory

class CustomizationSet {
    var text: String? = null
    var identifier: String? = null
    var price: Int = 0
    var hasPurchasable = false
    var customizations: MutableList<Customization> = mutableListOf()
    var ownedCustomizations: MutableList<Customization> = mutableListOf()

    fun isSetDeal(): Boolean {
        var total = 0
        for (customization in customizations) {
            if (!ownedCustomizations.contains(customization)) {
                customization.price?.let { total += it }
            }
        }
        return total >= price
    }
}
