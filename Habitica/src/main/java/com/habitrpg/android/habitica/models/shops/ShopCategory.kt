package com.habitrpg.android.habitica.models.shops

class ShopCategory {

    var identifier: String = ""
    var text: String = ""
    var notes: String = ""
    var purchaseAll: Boolean? = null

    var items: MutableList<ShopItem> = ArrayList()
}
