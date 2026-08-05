package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.local.InventoryLocalRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.inventory.Food
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.models.inventory.Item
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.android.habitica.models.responses.BuyResponse
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.user.Items
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf

class InventoryRepositoryImplTest :
    WordSpec({
        lateinit var repository: InventoryRepository
        val localRepository = mockk<InventoryLocalRepository>()
        val apiClient = mockk<ApiClient>()
        val authenticationHandler = mockk<AuthenticationHandler>()
        val appConfigManager = mockk<AppConfigManager>()
        beforeEach {
            every { authenticationHandler.currentUserID } returns "user-1"
            repository = InventoryRepositoryImpl(localRepository, apiClient, authenticationHandler, appConfigManager)
        }
        afterEach { clearAllMocks() }
        "retrieveInAppRewards" should {
            "save the rewards returned by the API" {
                val rewards = listOf(ShopItem())
                coEvery { apiClient.retrieveInAppRewards() } returns rewards
                every { localRepository.saveInAppRewards(rewards) } returns Unit
                val result = repository.retrieveInAppRewards()
                result shouldBe rewards
                verify { localRepository.saveInAppRewards(rewards) }
            }

            "do nothing when the API returns nothing" {
                coEvery { apiClient.retrieveInAppRewards() } returns null
                repository.retrieveInAppRewards()
                verify(exactly = 0) { localRepository.saveInAppRewards(any()) }
            }
        }
        "openMysteryItem" should {
            "mark the item owned and decrement the mystery count" {
                val equipment = Equipment().apply { key = "gear_1" }
                coEvery { apiClient.openMysteryItem() } returns equipment
                every { localRepository.getEquipment("gear_1") } returns flowOf(equipment)
                every { localRepository.markAsOwned(equipment, true) } returns Unit
                every { localRepository.decrementMysteryItemCount(null) } returns Unit
                val result = repository.openMysteryItem(null)
                result shouldBe equipment
                verify { localRepository.markAsOwned(equipment, true) }
                verify { localRepository.decrementMysteryItemCount(null) }
            }

            "return null when no matching equipment is found" {
                coEvery { apiClient.openMysteryItem() } returns null
                @Suppress("UNCHECKED_CAST")
                every { localRepository.getEquipment("") } returns flowOf<Equipment?>(null) as kotlinx.coroutines.flow.Flow<Equipment>
                val result = repository.openMysteryItem(null)
                result shouldBe null
                verify(exactly = 0) { localRepository.markAsOwned(any(), any()) }
            }
        }
        "sellItem by type/key" should {
            "return null when the owned item can't be found" {
                @Suppress("UNCHECKED_CAST")
                every { localRepository.getOwnedItem("user-1", "eggs", "key-1", true) } returns
                    flowOf<OwnedItem?>(null) as kotlinx.coroutines.flow.Flow<OwnedItem>
                val result = repository.sellItem("eggs", "key-1")
                result shouldBe null
            }

            "sell the owned item when found" {
                val ownedItem = OwnedItem().apply { key = "key-1"; itemType = "eggs"; numberOwned = 3 }
                val itemData = mockk<Item>()
                val user = User()
                every { localRepository.getOwnedItem("user-1", "eggs", "key-1", true) } returns flowOf(ownedItem)
                every { localRepository.getItem("eggs", "key-1") } returns flowOf(itemData)
                every { itemData.type } returns "eggs"
                every { itemData.key } returns "key-1"
                every { localRepository.setOwnedCount(ownedItem, 2) } returns Unit
                coEvery { apiClient.sellItem("eggs", "key-1") } returns user
                every { localRepository.soldItem("user-1", user) } returns user
                val result = repository.sellItem("eggs", "key-1")
                result shouldBe user
                verify { localRepository.setOwnedCount(ownedItem, 2) }
                verify { localRepository.soldItem("user-1", user) }
            }
        }
        "equip" should {
            "update the current mount locally and remotely" {
                val user = User().apply { items = Items() }
                every { localRepository.getLiveUser("user-1") } returns user
                every { localRepository.modify(user, any()) } answers { secondArg<(User) -> Unit>().invoke(user) }
                val returnedItems = Items().apply { currentMount = "mount_1" }
                coEvery { apiClient.equipItem("mount", "mount_1") } returns returnedItems
                val result = repository.equip("mount", "mount_1")
                result shouldBe returnedItems
                user.items?.currentMount shouldBe "mount_1"
            }

            "return null when there is no live user" {
                every { localRepository.getLiveUser("user-1") } returns null
                coEvery { apiClient.equipItem("mount", "mount_1") } returns Items()
                val result = repository.equip("mount", "mount_1")
                result shouldBe null
            }
        }
        "hatchPet" should {
            "apply the local change immediately when enabled" {
                val egg = Egg().apply { key = "egg-1" }
                val potion = HatchingPotion().apply { key = "potion-1" }
                every { appConfigManager.enableLocalChanges() } returns true
                every { localRepository.hatchPet("egg-1", "potion-1", "user-1") } returns Unit
                val items = Items()
                coEvery { apiClient.hatchPet("egg-1", "potion-1") } returns items
                every { localRepository.save(items, "user-1") } returns Unit
                var successCalls = 0
                val result = repository.hatchPet(egg, potion) { successCalls++ }
                result shouldBe items
                successCalls shouldBe 1
                verify { localRepository.hatchPet("egg-1", "potion-1", "user-1") }
            }

            "only call success after the API responds when local changes are disabled" {
                val egg = Egg().apply { key = "egg-1" }
                val potion = HatchingPotion().apply { key = "potion-1" }
                every { appConfigManager.enableLocalChanges() } returns false
                val items = Items()
                coEvery { apiClient.hatchPet("egg-1", "potion-1") } returns items
                every { localRepository.save(items, "user-1") } returns Unit
                var successCalls = 0
                repository.hatchPet(egg, potion) { successCalls++ }
                successCalls shouldBe 1
                verify(exactly = 0) { localRepository.hatchPet(any(), any(), any()) }
            }
        }
        "buyItem" should {
            "compute the gold delta locally when the API doesn't return a new gp value" {
                val user = User().apply { stats = Stats().apply { gp = 100.0 } }
                every { localRepository.getUnmanagedCopy(user) } returns user
                val response = BuyResponse()
                coEvery { apiClient.buyItem("gear_1", 2) } returns response
                every { localRepository.save(user) } returns Unit
                val result = repository.buyItem(user, "gear_1", 10.0, 2)
                result shouldBe response
                user.stats?.gp shouldBe 80.0
                verify { localRepository.save(user) }
            }

            "return null when the API returns nothing" {
                coEvery { apiClient.buyItem("gear_1", 1) } returns null
                val result = repository.buyItem(User(), "gear_1", 10.0, 1)
                result shouldBe null
            }
        }
        "purchaseItem" should {
            "increment gems bought only for gem purchases" {
                coEvery { apiClient.purchaseItem("gems", "gem", 5) } returns null
                every { localRepository.incrementGemsBought("user-1", 5) } returns Unit
                repository.purchaseItem("gems", "gem", 5)
                verify { localRepository.incrementGemsBought("user-1", 5) }
            }

            "not touch gems bought for non-gem purchases" {
                coEvery { apiClient.purchaseItem("marketGear", "armor_1", 1) } returns null
                repository.purchaseItem("marketGear", "armor_1", 1)
                verify(exactly = 0) { localRepository.incrementGemsBought(any(), any()) }
            }
        }
    })
