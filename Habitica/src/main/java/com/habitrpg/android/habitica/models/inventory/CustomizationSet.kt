package com.habitrpg.android.habitica.models.inventory

import com.habitrpg.shared.habitica.models.inventory.Customization

class CustomizationSet {
    var text: String? = null
    var identifier: String? = null
    var price: Int = 0
    var hasPurchasable = false
    var customizations: MutableList<Customization> = mutableListOf()
}