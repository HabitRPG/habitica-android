package com.habitrpg.android.habitica.ui.viewmodels.inventory.shops

import android.content.Context
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.shops.ShopCategory
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SeasonalShopViewModel @Inject constructor(
    inventoryRepository: InventoryRepository,
    configManager: AppConfigManager,
    userRepository: UserRepository,
    userViewModel: MainUserViewModel
) : ShopViewModel(inventoryRepository, configManager, userRepository, userViewModel) {

    override suspend fun retrieveShopInventory(context: Context): Shop {
        val newShop = super.retrieveShopInventory(context)
        newShop.categories.sortWith(
            compareBy<ShopCategory> { it.items.firstOrNull()?.currency != "gold" }
                .thenByDescending {
                    if (it.identifier ==
                        "quests"
                    ) {
                        10000
                    } else {
                        findReleaseYear(
                            it.items.firstOrNull()?.key ?: ""
                        )
                    }
                }.thenBy { it.items.firstOrNull()?.locked },
        )
        return newShop
    }

    private fun findReleaseYear(key: String): Int {
        val result = key.filter { it.isDigit() }
        return if (result.isEmpty()) {
            2014
        } else {
            result.toInt()
        }
    }
}
