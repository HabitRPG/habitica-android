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
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

class TimeTravelersShopViewModelTest :
    WordSpec({
        val inventoryRepository = mockk<InventoryRepository>()
        val configManager = mockk<AppConfigManager>()
        val userRepository = mockk<UserRepository>()
        val mainUserViewModel = mockk<MainUserViewModel>()
        val context = mockk<Context>(relaxed = true)

        every { configManager.shopSpriteSuffix() } returns null
        every { inventoryRepository.getInAppReward(any()) } returns flowOf(ShopItem())
        every { inventoryRepository.getArmoireRemainingCount() } returns flowOf(0)
        every { inventoryRepository.getOwnedItems(any<Boolean>()) } returns flowOf(emptyMap())
        every { inventoryRepository.getInAppRewards() } returns flowOf(emptyList())
        every { context.getString(R.string.mystery_sets) } returns "Mystery Sets"

        fun makeViewModel() =
            TimeTravelersShopViewModel(inventoryRepository, configManager, userRepository, mainUserViewModel).apply {
                shopIdentifier = Shop.TIME_TRAVELERS_SHOP
            }

        "retrieveShopInventory" should {
            "leave non mystery-set categories untouched" {
                val normalCategory =
                    ShopCategory().apply {
                        identifier = "gear"
                        pinType = "gear"
                        items.add(ShopItem().apply { key = "sword" })
                    }
                val shop = Shop().apply { categories.add(normalCategory) }
                coEvery { inventoryRepository.retrieveShopInventory("time-travelers") } returns shop

                val result = makeViewModel().retrieveShopInventory(context)

                result.categories.first().identifier shouldBe "gear"
            }

            "group mystery_set categories into a single mystery_sets category" {
                val setOne =
                    ShopCategory().apply {
                        identifier = "set_one"
                        text = "Set One"
                        pinType = "mystery_set"
                        items.add(ShopItem().apply { key = "item_one" })
                    }
                val setTwo =
                    ShopCategory().apply {
                        identifier = "set_two"
                        text = "Set Two"
                        pinType = "mystery_set"
                        items.add(ShopItem().apply { key = "item_two" })
                    }
                val shop =
                    Shop().apply {
                        categories.add(setOne)
                        categories.add(setTwo)
                    }
                coEvery { inventoryRepository.retrieveShopInventory("time-travelers") } returns shop

                val result = makeViewModel().retrieveShopInventory(context)

                result.categories.size shouldBe 1
                val mysteryCategory = result.categories.first()
                mysteryCategory.identifier shouldBe "mystery_sets"
                mysteryCategory.items.map { it.key } shouldBe listOf("set_one", "set_two")
                mysteryCategory.items.map { it.pinType } shouldBe listOf("mystery_set", "mystery_set")
                mysteryCategory.items.map { it.path } shouldBe listOf("mystery.set_one", "mystery.set_two")
            }

            "add an empty mystery_sets category when there are no mystery sets" {
                val normalCategory =
                    ShopCategory().apply {
                        identifier = "gear"
                        pinType = "gear"
                        items.add(ShopItem().apply { key = "sword" })
                    }
                val shop = Shop().apply { categories.add(normalCategory) }
                coEvery { inventoryRepository.retrieveShopInventory("time-travelers") } returns shop

                val result = makeViewModel().retrieveShopInventory(context)

                result.categories.map { it.identifier } shouldBe listOf("gear", "mystery_sets")
                result.categories.last().items shouldBe emptyList()
            }
        }
    })
