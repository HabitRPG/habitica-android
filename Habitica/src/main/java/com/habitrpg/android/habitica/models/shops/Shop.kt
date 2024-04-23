package com.habitrpg.android.habitica.models.shops

import android.content.Context
import com.habitrpg.android.habitica.R

class Shop {
    var identifier: String = ""
    var text: String = ""
    var notes: String = ""
    var imageName: String = ""

    var categories: MutableList<ShopCategory> = ArrayList()

    val npcNameResource: Int
        get() =
            when (identifier) {
                MARKET -> R.string.market_owner
                QUEST_SHOP -> R.string.questShop_owner
                SEASONAL_SHOP -> R.string.seasonalShop_owner
                TIME_TRAVELERS_SHOP -> R.string.timetravelers_owner
                CUSTOMIZATIONS -> R.string.customizations_owner
                else -> R.string.market_owner
            }

    fun getNpcName(context: Context): String = context.getString(npcNameResource)

    companion object {
        const val MARKET = "market"
        const val QUEST_SHOP = "questShop"
        const val TIME_TRAVELERS_SHOP = "timeTravelersShop"
        const val SEASONAL_SHOP = "seasonalShop"
        const val CUSTOMIZATIONS = "customizationsShop"
    }
}
