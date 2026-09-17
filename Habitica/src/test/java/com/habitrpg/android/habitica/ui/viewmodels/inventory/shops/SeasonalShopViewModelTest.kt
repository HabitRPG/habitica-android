package com.habitrpg.android.habitica.ui.viewmodels.inventory.shops

import android.content.Context
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

class SeasonalShopViewModelTest :
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

        fun makeViewModel() =
            SeasonalShopViewModel(inventoryRepository, configManager, userRepository, mainUserViewModel).apply {
                shopIdentifier = Shop.SEASONAL_SHOP
            }

        fun category(
            identifier: String,
            currency: String,
            key: String,
            locked: Boolean = false,
        ) = ShopCategory().apply {
            this.identifier = identifier
            items.add(
                ShopItem().apply {
                    this.currency = currency
                    this.key = key
                    this.locked = locked
                },
            )
        }

        "retrieveShopInventory" should {
            "put gold categories before gem categories" {
                val goldCategory = category("gold_cat", "gold", "item2018")
                val gemCategory = category("gem_cat", "gems", "item2020")
                val shop =
                    Shop().apply {
                        categories.add(gemCategory)
                        categories.add(goldCategory)
                    }
                coEvery { inventoryRepository.retrieveShopInventory("seasonal") } returns shop

                val result = makeViewModel().retrieveShopInventory(context)

                result.categories.map { it.identifier } shouldBe listOf("gold_cat", "gem_cat")
            }

            "sort gold categories by release year, newest first" {
                val older = category("older", "gold", "item2018")
                val newer = category("newer", "gold", "item2020")
                val shop =
                    Shop().apply {
                        categories.add(older)
                        categories.add(newer)
                    }
                coEvery { inventoryRepository.retrieveShopInventory("seasonal") } returns shop

                val result = makeViewModel().retrieveShopInventory(context)

                result.categories.map { it.identifier } shouldBe listOf("newer", "older")
            }

            "always sort the quests category first among gold categories" {
                val newest = category("newest", "gold", "item2022")
                val quests = category("quests", "gold", "item2001")
                val shop =
                    Shop().apply {
                        categories.add(newest)
                        categories.add(quests)
                    }
                coEvery { inventoryRepository.retrieveShopInventory("seasonal") } returns shop

                val result = makeViewModel().retrieveShopInventory(context)

                result.categories.map { it.identifier } shouldBe listOf("quests", "newest")
            }

            "treat items without a release year as 2014" {
                val undated = category("undated", "gold", "no_digits_here")
                val dated = category("dated", "gold", "item2013")
                val shop =
                    Shop().apply {
                        categories.add(dated)
                        categories.add(undated)
                    }
                coEvery { inventoryRepository.retrieveShopInventory("seasonal") } returns shop

                val result = makeViewModel().retrieveShopInventory(context)

                result.categories.map { it.identifier } shouldBe listOf("undated", "dated")
            }
        }
    })
