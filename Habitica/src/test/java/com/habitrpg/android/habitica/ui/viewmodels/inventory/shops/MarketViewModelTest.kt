package com.habitrpg.android.habitica.ui.viewmodels.inventory.shops

import android.content.Context
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.user.Flags
import com.habitrpg.android.habitica.models.user.Purchases
import com.habitrpg.android.habitica.models.user.Stats
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

class MarketViewModelTest :
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
        coEvery { inventoryRepository.retrieveShopInventory("market") } returns Shop()

        fun makeViewModel() =
            MarketViewModel(inventoryRepository, configManager, userRepository, mainUserViewModel).apply {
                shopIdentifier = Shop.MARKET
            }

        afterEach { clearMocks(mainUserViewModel.user) }

        "retrieveShopInventory" should {
            "add a special category with the gem and fortify items" {
                every { mainUserViewModel.user.value } returns User()
                val shop = makeViewModel().retrieveShopInventory(context)
                val specialCategory = shop.categories.last()
                specialCategory.items.map { it.key } shouldBe listOf("gem", "fortify")
            }

            "mark the gem item as unlimited when the user is not subscribed" {
                every { mainUserViewModel.user.value } returns User()
                val shop = makeViewModel().retrieveShopInventory(context)
                val gemItem =
                    shop.categories
                        .last()
                        .items
                        .first { it.key == "gem" }
                gemItem.limitedNumberLeft shouldBe -1
            }

            "use the user's remaining gems when subscribed" {
                val user =
                    User().apply {
                        purchased =
                            Purchases().apply {
                                plan =
                                    SubscriptionPlan().apply {
                                        customerId = "abc"
                                        active = true
                                        gemsBought = 4
                                    }
                            }
                    }
                every { mainUserViewModel.user.value } returns user
                val shop = makeViewModel().retrieveShopInventory(context)
                val gemItem =
                    shop.categories
                        .last()
                        .items
                        .first { it.key == "gem" }
                gemItem.limitedNumberLeft shouldBe user.purchased?.plan?.numberOfGemsLeft
            }

            "not add a rebirth item if rebirth is not enabled" {
                every { mainUserViewModel.user.value } returns User()
                val shop = makeViewModel().retrieveShopInventory(context)
                val specialCategory = shop.categories.last()
                specialCategory.items.map { it.key } shouldBe listOf("gem", "fortify")
            }

            "add a rebirth item if rebirth is enabled" {
                val user = User().apply { flags = Flags().apply { rebirthEnabled = true } }
                every { mainUserViewModel.user.value } returns user
                val shop = makeViewModel().retrieveShopInventory(context)
                val specialCategory = shop.categories.last()
                specialCategory.items.map { it.key } shouldBe listOf("gem", "fortify", "rebirth_orb")
            }

            "add a free rebirth note when the user is level 50-99 and has not had a recent free rebirth" {
                every { context.getString(R.string.free_rebirth_at_level_100) } returns "Free rebirth at level 100"
                val user =
                    User().apply {
                        flags =
                            Flags().apply {
                                rebirthEnabled = true
                                lastFreeRebirth = null
                            }
                        stats = Stats().apply { lvl = 60 }
                    }
                every { mainUserViewModel.user.value } returns user
                val shop = makeViewModel().retrieveShopInventory(context)
                val specialCategory = shop.categories.last()
                specialCategory.notes shouldBe "Free rebirth at level 100"
            }

            "not add a free rebirth note when the user already had a recent free rebirth" {
                val user =
                    User().apply {
                        flags =
                            Flags().apply {
                                rebirthEnabled = true
                                lastFreeRebirth = Date()
                            }
                        stats = Stats().apply { lvl = 60 }
                    }
                every { mainUserViewModel.user.value } returns user
                val shop = makeViewModel().retrieveShopInventory(context)
                val specialCategory = shop.categories.last()
                specialCategory.notes shouldBe ""
            }

            "not add a free rebirth note when the user is outside the 50-99 level range" {
                val user =
                    User().apply {
                        flags =
                            Flags().apply {
                                rebirthEnabled = true
                                lastFreeRebirth = null
                            }
                        stats = Stats().apply { lvl = 100 }
                    }
                every { mainUserViewModel.user.value } returns user
                val shop = makeViewModel().retrieveShopInventory(context)
                val specialCategory = shop.categories.last()
                specialCategory.notes shouldBe ""
            }
        }
    })
