package com.habitrpg.android.habitica.ui.viewmodels.inventory.shops

import android.content.Context
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.shops.ShopCategory
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TimeTravelersShopViewModel @Inject constructor(
    inventoryRepository: InventoryRepository,
    configManager: AppConfigManager,
    userRepository: UserRepository,
    userViewModel: MainUserViewModel
) : ShopViewModel(inventoryRepository, configManager, userRepository, userViewModel) {

    override suspend fun retrieveShopInventory(context: Context): Shop {
        val shop = super.retrieveShopInventory(context)
        val newCategories = mutableListOf<ShopCategory>()
        for (category in shop.categories) {
            if (category.pinType != "mystery_set") {
                newCategories.add(category)
            } else {
                val newCategory = newCategories.find { it.identifier == "mystery_sets" } ?: ShopCategory()
                if (newCategory.identifier.isEmpty()) {
                    newCategory.identifier = "mystery_sets"
                    newCategory.text = context.getString(R.string.mystery_sets)
                    newCategories.add(newCategory)
                }
                val item = category.items.firstOrNull() ?: continue
                item.key = category.identifier
                item.text = category.text
                item.imageName = "shop_set_mystery_${item.key}"
                item.pinType = "mystery_set"
                item.path = "mystery.${item.key}"
                newCategory.items.add(item)
            }
        }
        val mysterySetCategory = newCategories.find { it.identifier == "mystery_sets" } ?: ShopCategory()
        if (mysterySetCategory.identifier.isEmpty()) {
            mysterySetCategory.identifier = "mystery_sets"
            mysterySetCategory.text = context.getString(R.string.mystery_sets)
            newCategories.add(mysterySetCategory)
        }
        shop.categories = newCategories
        return shop
    }
}
