package com.habitrpg.android.habitica.ui.viewmodels.inventory.shops

import android.content.Context
import androidx.core.os.bundleOf
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.shops.ShopCategory
import com.habitrpg.android.habitica.ui.viewmodels.BaseViewModel
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.common.habitica.helpers.MainNavigationController
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

abstract class ShopViewModel(
    private val inventoryRepository: InventoryRepository,
    configManager: AppConfigManager,
    userRepository: UserRepository,
    userViewModel: MainUserViewModel
) : BaseViewModel(userRepository, userViewModel) {

    var shopIdentifier: String? = null

    val shopSpriteSuffix = configManager.shopSpriteSuffix()

    val armoireItem = inventoryRepository.getInAppReward("armoire")
    val armoireCount = inventoryRepository.getArmoireRemainingCount()

    val ownedItems = inventoryRepository.getOwnedItems()
    val inAppRewards = inventoryRepository.getInAppRewards()
        .map { rewards -> rewards.map { it.key } }

    val isUserSubscribed: Boolean
        get() = userViewModel.user.value?.isSubscribed == true

    fun onEmptySectionTapped(sectionIdentifier: String) {
        if (shopIdentifier == Shop.CUSTOMIZATIONS) {
            var navigationID = R.id.ComposeAvatarCustomizationFragment
            var type: String
            var category = ""
            when (sectionIdentifier) {
                "color" -> {
                    type = "hair"
                    category = "color"
                }
                "facialHair" -> {
                    type = "hair"
                    category = "beard"
                }
                "base" -> {
                    type = "hair"
                    category = "base"
                }
                "animalEars" -> {
                    navigationID = R.id.composeAvatarEquipmentFragment
                    type = "headAccessory"
                    category = "animal"
                }
                "animalTails" -> {
                    navigationID = R.id.composeAvatarEquipmentFragment
                    type = "back"
                    category = "animal"
                }
                "backgrounds" -> {
                    type = "background"
                }
                else -> {
                    type = sectionIdentifier
                }
            }
            MainNavigationController.navigate(navigationID, bundleOf("category" to category, "type" to type))
        } else if (shopIdentifier == Shop.TIME_TRAVELERS_SHOP) {
            MainNavigationController.navigate(R.id.equipmentOverviewFragment)
        }
    }

    open suspend fun retrieveShopInventory(context: Context): Shop {
        val shopUrl =
            when (this.shopIdentifier) {
                Shop.MARKET -> "market"
                Shop.QUEST_SHOP -> "quests"
                Shop.TIME_TRAVELERS_SHOP -> "time-travelers"
                Shop.SEASONAL_SHOP -> "seasonal"
                Shop.CUSTOMIZATIONS -> "customizations"
                else -> ""
            }

        val newShop = inventoryRepository.retrieveShopInventory(shopUrl) ?: return Shop()
        newShop.categories.forEach { category ->
            if (category.endDate == null) {
                category.endDate = category.items.firstOrNull { it.availableUntil != null }?.availableUntil
            }
        }
        return newShop
    }

    suspend fun retrieveMarketGear(): List<ShopCategory>? {
        val shop = inventoryRepository.retrieveMarketGear()
        val equipment =
            inventoryRepository
                .getOwnedEquipment()
                .map { equipment -> equipment.map { it.key } }
                .firstOrNull()
        for (category in shop?.categories ?: emptyList()) {
            val items =
                category.items
                    .asSequence()
                    .filter {
                        equipment?.contains(it.key) == false
                    }.sortedBy { it.locked }
                    .toList()
            category.items.clear()
            category.items.addAll(items)
        }
        return shop?.categories
    }
}
