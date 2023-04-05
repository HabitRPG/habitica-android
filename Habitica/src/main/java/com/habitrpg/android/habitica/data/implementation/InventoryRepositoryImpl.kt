@file:OptIn(ExperimentalCoroutinesApi::class)

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
import com.habitrpg.android.habitica.models.inventory.Mount
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.responses.BuyResponse
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.user.Items
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.OwnedMount
import com.habitrpg.android.habitica.models.user.OwnedPet
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import com.habitrpg.shared.habitica.models.responses.FeedResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest

class InventoryRepositoryImpl(
    localRepository: InventoryLocalRepository,
    apiClient: ApiClient,
    authenticationHandler: AuthenticationHandler,
    var appConfigManager: AppConfigManager
) : BaseRepositoryImpl<InventoryLocalRepository>(localRepository, apiClient, authenticationHandler), InventoryRepository {
    override fun getQuestContent(keys: List<String>) = localRepository.getQuestContent(keys)

    override fun getQuestContent(key: String) = localRepository.getQuestContent(key)

    override fun getEquipment(searchedKeys: List<String>): Flow<List<Equipment>> {
        return localRepository.getEquipment(searchedKeys)
    }

    override fun getArmoireRemainingCount(): Flow<Int> {
        return localRepository.getArmoireRemainingCount()
    }

    override fun getInAppRewards(): Flow<List<ShopItem>> {
        return localRepository.getInAppRewards()
    }

    override fun getInAppReward(key : String) : Flow<ShopItem> {
        return localRepository.getInAppReward(key)
    }

    override suspend fun retrieveInAppRewards(): List<ShopItem>? {
        val rewards = apiClient.retrieveInAppRewards()
        if (rewards != null) {
            localRepository.saveInAppRewards(rewards)
        }
        return rewards
    }

    override fun getOwnedEquipment(type: String): Flow<List<Equipment>> {
        return localRepository.getOwnedEquipment(type)
    }

    override fun getOwnedEquipment(): Flow<List<Equipment>> {
        return localRepository.getOwnedEquipment()
    }

    override fun getEquipmentType(type: String, set: String): Flow<List<Equipment>> {
        return localRepository.getEquipmentType(type, set)
    }

    override fun getOwnedItems(itemType: String, includeZero: Boolean): Flow<List<OwnedItem>> {
        return authenticationHandler.userIDFlow.flatMapLatest { localRepository.getOwnedItems(itemType, it, includeZero) }
    }

    override fun getOwnedItems(includeZero: Boolean): Flow<Map<String, OwnedItem>> {
        return authenticationHandler.userIDFlow.flatMapLatest { localRepository.getOwnedItems(it, includeZero) }
    }

    override fun getItems(itemClass: Class<out Item>, keys: Array<String>): Flow<List<Item>> {
        return localRepository.getItems(itemClass, keys)
    }

    override fun getItems(itemClass: Class<out Item>): Flow<List<Item>> {
        return localRepository.getItems(itemClass)
    }

    override fun getEquipment(key: String): Flow<Equipment> {
        return localRepository.getEquipment(key)
    }

    override suspend fun openMysteryItem(user: User?): Equipment? {
        val item = apiClient.openMysteryItem()
        val equipment = localRepository.getEquipment(item?.key ?: "").firstOrNull() ?: return null
        val liveEquipment = localRepository.getLiveObject(equipment)
        localRepository.executeTransaction {
            liveEquipment?.owned = true
        }
        localRepository.decrementMysteryItemCount(user)
        return equipment
    }

    override fun saveEquipment(equipment: Equipment) {
        localRepository.save(equipment)
    }

    override fun getMounts(): Flow<List<Mount>> {
        return localRepository.getMounts()
    }

    override fun getMounts(type: String?, group: String?, color: String?): Flow<List<Mount>> {
        return localRepository.getMounts(type, group, color)
    }

    override fun getOwnedMounts(): Flow<List<OwnedMount>> {
        return authenticationHandler.userIDFlow.flatMapLatest {  localRepository.getOwnedMounts(it) }
    }

    override fun getPets(): Flow<List<Pet>> {
        return localRepository.getPets()
    }

    override fun getPets(type: String?, group: String?, color: String?): Flow<List<Pet>> {
        return localRepository.getPets(type, group, color)
    }

    override fun getOwnedPets(): Flow<List<OwnedPet>> {
        return authenticationHandler.userIDFlow.flatMapLatest {  localRepository.getOwnedPets(it) }
    }

    override fun updateOwnedEquipment(user: User) {
        localRepository.updateOwnedEquipment(user)
    }

    override suspend fun changeOwnedCount(type: String, key: String, amountToAdd: Int) {
        localRepository.changeOwnedCount(type, key, currentUserID, amountToAdd)
    }

    override suspend fun sellItem(type: String, key: String): User? {
        val item = localRepository.getOwnedItem(currentUserID, type, key, true).firstOrNull() ?: return null
        return sellItem(item)
    }

    override suspend fun sellItem(item: OwnedItem): User? {
        val itemData = localRepository.getItem(item.itemType ?: "", item.key ?: "").firstOrNull() ?: return null
        return sellItem(itemData, item)
    }

    override fun getLatestMysteryItem(): Flow<Equipment> {
        return localRepository.getLatestMysteryItem()
    }

    override fun getItem(type: String, key: String): Flow<Item> {
        return localRepository.getItem(type, key)
    }

    private suspend fun sellItem(item: Item, ownedItem: OwnedItem): User? {
        localRepository.executeTransaction {
            val liveItem = localRepository.getLiveObject(ownedItem)
            liveItem?.numberOwned = (liveItem?.numberOwned ?: 0) - 1
        }
        val user = apiClient.sellItem(item.type, item.key) ?: return null
        return localRepository.soldItem(currentUserID, user)
    }

    override suspend fun equipGear(equipment: String, asCostume: Boolean): Items? {
        return equip(if (asCostume) "costume" else "equipped", equipment)
    }

    override suspend fun equip(type: String, key: String): Items? {
        val liveUser = localRepository.getLiveUser(currentUserID)

        if (liveUser != null) {
            localRepository.modify(liveUser) { user ->
                if (type == "mount") {
                    user.items?.currentMount = key
                } else if (type == "pet") {
                    user.items?.currentPet = key
                }
                val outfit = if (type == "costume") {
                    user.items?.gear?.costume
                } else {
                    user.items?.gear?.equipped
                }
                when (key.split("_").firstOrNull()) {
                    "weapon" -> outfit?.weapon = key
                    "armor" -> outfit?.armor = key
                    "shield" -> outfit?.shield = key
                    "eyewear" -> outfit?.eyeWear = key
                    "head" -> outfit?.head = key
                    "back" -> outfit?.back = key
                    "headAccessory" -> outfit?.headAccessory = key
                    "body" -> outfit?.body = key
                }
            }
        }
        val items = apiClient.equipItem(type, key) ?: return null
        if (liveUser == null) return null
        localRepository.modify(liveUser) { liveUser ->
            val newEquipped = items.gear?.equipped
            val oldEquipped = liveUser.items?.gear?.equipped
            val newCostume = items.gear?.costume
            val oldCostume = liveUser.items?.gear?.costume
            newEquipped?.let { equipped -> oldEquipped?.updateWith(equipped) }
            newCostume?.let { costume -> oldCostume?.updateWith(costume) }
            liveUser.items?.currentMount = items.currentMount
            liveUser.items?.currentPet = items.currentPet
            liveUser.balance = liveUser.balance
        }
        return items
    }

    override suspend fun feedPet(pet: Pet, food: Food): FeedResponse? {
        val feedResponse = apiClient.feedPet(pet.key ?: "", food.key) ?: return null
        localRepository.feedPet(food.key, pet.key ?: "", feedResponse.value ?: 0, currentUserID)
        return feedResponse
    }

    override suspend fun hatchPet(egg: Egg, hatchingPotion: HatchingPotion, successFunction: () -> Unit): Items? {
        if (appConfigManager.enableLocalChanges()) {
            localRepository.hatchPet(egg.key, hatchingPotion.key, currentUserID)
            successFunction()
        }
        val items = apiClient.hatchPet(egg.key, hatchingPotion.key) ?: return null
        localRepository.save(items, currentUserID)
        if (!appConfigManager.enableLocalChanges()) {
            successFunction()
        }
        return items
    }

    override suspend fun inviteToQuest(quest: QuestContent): Quest? {
        val newQuest = apiClient.inviteToQuest("party", quest.key)
        localRepository.changeOwnedCount("quests", quest.key, currentUserID, -1)
        return newQuest
    }

    override suspend fun buyItem(user: User?, id: String, value: Double, purchaseQuantity: Int): BuyResponse? {
        val buyResponse = apiClient.buyItem(id, purchaseQuantity) ?: return null
        val foundUser = user ?: localRepository.getLiveUser(currentUserID) ?: return buyResponse
        val copiedUser = localRepository.getUnmanagedCopy(foundUser)
        if (buyResponse.items != null) {
            copiedUser.items = buyResponse.items
        }
        if (buyResponse.hp != null) {
            copiedUser.stats?.hp = buyResponse.hp
        }
        if (buyResponse.exp != null) {
            copiedUser.stats?.exp = buyResponse.exp
        }
        if (buyResponse.mp != null) {
            copiedUser.stats?.mp = buyResponse.mp
        }
        if (buyResponse.gp != null) {
            copiedUser.stats?.gp = buyResponse.gp
        } else {
            copiedUser.stats?.gp = (copiedUser.stats?.gp ?: 0.0) - (value * purchaseQuantity)
        }
        if (buyResponse.lvl != null) {
            copiedUser.stats?.lvl = buyResponse.lvl
        }
        localRepository.save(copiedUser)
        return buyResponse
    }

    override fun getAvailableLimitedItems(): Flow<List<Item>> {
        return localRepository.getAvailableLimitedItems()
    }

    override suspend fun retrieveShopInventory(identifier: String): Shop? {
        return apiClient.retrieveShopIventory(identifier)
    }

    override suspend fun retrieveMarketGear(): Shop? {
        return apiClient.retrieveMarketGear()
    }

    override suspend fun purchaseMysterySet(categoryIdentifier: String): Void? {
        return apiClient.purchaseMysterySet(categoryIdentifier)
    }

    override suspend fun purchaseHourglassItem(purchaseType: String, key: String): Void? {
        return apiClient.purchaseHourglassItem(purchaseType, key)
    }

    override suspend fun purchaseQuest(key: String): Void? {
        return apiClient.purchaseQuest(key)
    }

    override suspend fun purchaseSpecialSpell(key: String): Void? {
        return apiClient.purchaseSpecialSpell(key)
    }

    override suspend fun purchaseItem(purchaseType: String, key: String, purchaseQuantity: Int): Void? {
        return apiClient.purchaseItem(purchaseType, key, purchaseQuantity)
    }

    override suspend fun togglePinnedItem(item: ShopItem): List<ShopItem>? {
        if (item.isValid) {
            apiClient.togglePinnedItem(item.pinType ?: "", item.path ?: "")
        }
        return retrieveInAppRewards()
    }
}
