package com.habitrpg.android.habitica.ui.viewmodels.inventory.shops

import android.content.Context
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.shops.ShopCategory
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.user.Purchases
import com.habitrpg.android.habitica.models.user.SubscriptionPlan
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import java.util.Date

class ShopViewModelTest :
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

        fun makeViewModel(): ShopViewModel =
            object : ShopViewModel(inventoryRepository, configManager, userRepository, mainUserViewModel) {}

        afterEach { clearMocks(mainUserViewModel.user) }

        "isUserSubscribed" should {
            "return true if the user is subscribed" {
                every { mainUserViewModel.user.value } returns
                    User().apply {
                        purchased =
                            Purchases().apply {
                                plan =
                                    SubscriptionPlan().apply {
                                        customerId = "abc"
                                        active = true
                                    }
                            }
                    }
                makeViewModel().isUserSubscribed shouldBe true
            }

            "return false if the user is not subscribed" {
                every { mainUserViewModel.user.value } returns User()
                makeViewModel().isUserSubscribed shouldBe false
            }

            "return false if there is no user" {
                every { mainUserViewModel.user.value } returns null
                makeViewModel().isUserSubscribed shouldBe false
            }
        }

        "retrieveShopInventory" should {
            "return an empty shop if the repository has none" {
                coEvery { inventoryRepository.retrieveShopInventory(any()) } returns null
                val viewModel = makeViewModel()
                viewModel.shopIdentifier = Shop.MARKET
                val shop = viewModel.retrieveShopInventory(context)
                shop.categories shouldBe emptyList()
            }

            "fill in a missing category end date from its items' availableUntil date" {
                val availableUntil = Date()
                val category =
                    ShopCategory().apply {
                        endDate = null
                        items.add(ShopItem().apply { this.endDate = availableUntil })
                    }
                val shop = Shop().apply { categories.add(category) }
                coEvery { inventoryRepository.retrieveShopInventory("market") } returns shop
                val viewModel = makeViewModel()
                viewModel.shopIdentifier = Shop.MARKET
                val result = viewModel.retrieveShopInventory(context)
                result.categories.first().endDate shouldBe availableUntil
            }

            "not overwrite an existing category end date" {
                val existingDate = Date(0)
                val category =
                    ShopCategory().apply {
                        endDate = existingDate
                        items.add(ShopItem().apply { this.endDate = Date() })
                    }
                val shop = Shop().apply { categories.add(category) }
                coEvery { inventoryRepository.retrieveShopInventory("quests") } returns shop
                val viewModel = makeViewModel()
                viewModel.shopIdentifier = Shop.QUEST_SHOP
                val result = viewModel.retrieveShopInventory(context)
                result.categories.first().endDate shouldBe existingDate
            }
        }

        "retrieveMarketGear" should {
            "filter out already owned equipment and sort locked items last" {
                val lockedItem =
                    ShopItem().apply {
                        key = "locked_item"
                        locked = true
                    }
                val unlockedItem =
                    ShopItem().apply {
                        key = "unlocked_item"
                        locked = false
                    }
                val ownedItem =
                    ShopItem().apply {
                        key = "owned_item"
                        locked = false
                    }
                val category =
                    ShopCategory().apply {
                        items.add(lockedItem)
                        items.add(unlockedItem)
                        items.add(ownedItem)
                    }
                val shop = Shop().apply { categories.add(category) }
                coEvery { inventoryRepository.retrieveMarketGear() } returns shop
                every { inventoryRepository.getOwnedEquipment() } returns
                    flowOf(listOf(Equipment().apply { key = "owned_item" }))

                val result = makeViewModel().retrieveMarketGear()

                val items = result?.first()?.items
                items?.map { it.key } shouldBe listOf("unlocked_item", "locked_item")
            }
        }
    })
