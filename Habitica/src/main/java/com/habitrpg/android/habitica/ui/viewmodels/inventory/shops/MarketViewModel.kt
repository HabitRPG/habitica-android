package com.habitrpg.android.habitica.ui.viewmodels.inventory.shops

import android.content.Context
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.shops.ShopCategory
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MarketViewModel @Inject constructor(
    inventoryRepository: InventoryRepository,
    configManager: AppConfigManager,
    userRepository: UserRepository,
    userViewModel: MainUserViewModel
) : ShopViewModel(inventoryRepository, configManager, userRepository, userViewModel) {

    override suspend fun retrieveShopInventory(context: Context): Shop {
        val shop = super.retrieveShopInventory(context)
        val user = userViewModel.user.value
        val specialCategory = ShopCategory()
        specialCategory.text = context.getString(R.string.special)
        val item = ShopItem.makeGemItem(context.resources)
        if (user?.isSubscribed == true) {
            item.limitedNumberLeft = user.purchased?.plan?.numberOfGemsLeft
        } else {
            item.limitedNumberLeft = -1
        }
        specialCategory.items.add(item)
        specialCategory.items.add(ShopItem.makeFortifyItem(context.resources))
        if (user?.flags?.rebirthEnabled == true) {
            specialCategory.items.add(ShopItem.makeRebirthItem(context.resources, user))

            val userLevel = user.stats?.lvl ?: 0
            if (userLevel in 50..99) {
                val lastFreeRebirth = user.flags?.lastFreeRebirth
                if (lastFreeRebirth == null ||
                    (Date().time - lastFreeRebirth.time) / (1000 * 60 * 60 * 24) >= 45
                ) {
                    specialCategory.notes = context.getString(R.string.free_rebirth_at_level_100)
                }
            }
        }
        shop.categories.add(specialCategory)
        return shop
    }
}
