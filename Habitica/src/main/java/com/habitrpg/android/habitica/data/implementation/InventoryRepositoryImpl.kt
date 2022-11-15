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
import com.habitrpg.shared.habitica.models.responses.FeedResponse
import io.reactivex.rxjava3.core.Flowable
import kotlinx.coroutines.flow.Flow

class InventoryRepositoryImpl(
    localRepository: InventoryLocalRepository,
    apiClient: ApiClient,
    userID: String,
    var appConfigManager: AppConfigManager
) : BaseRepositoryImpl<InventoryLocalRepository>(localRepository, apiClient, userID), InventoryRepository {
    override fun getQuestContent(keys: List<String>) = localRepository.getQuestContent(keys)

    override fun getQuestContent(key: String) = localRepository.getQuestContent(key)

    override fun getEquipment(searchedKeys: List<String>): Flowable<out List<Equipment>> {
        return localRepository.getEquipment(searchedKeys)
    }

    override fun getArmoireRemainingCount(): Long {
        return localRepository.getArmoireRemainingCount()
    }

    override fun getInAppRewards(): Flowable<out List<ShopItem>> {
        return localRepository.getInAppRewards()
    }

    override fun retrieveInAppRewards(): Flowable<List<ShopItem>> {
        return apiClient.retrieveInAppRewards().doOnNext { localRepository.saveInAppRewards(it) }
    }

    override fun getOwnedEquipment(type: String): Flowable<out List<Equipment>> {
        return localRepository.getOwnedEquipment(type)
    }

    override fun getOwnedEquipment(): Flowable<out List<Equipment>> {
        return localRepository.getOwnedEquipment()
    }

    override fun getEquipmentType(type: String, set: String): Flowable<out List<Equipment>> {
        return localRepository.getEquipmentType(type, set)
    }

    override fun getOwnedItems(itemType: String, includeZero: Boolean): Flow<List<OwnedItem>> {
        return localRepository.getOwnedItems(itemType, userID, includeZero)
    }

    override fun getOwnedItems(includeZero: Boolean): Flowable<Map<String, OwnedItem>> {
        return localRepository.getOwnedItems(userID, includeZero)
    }

    override fun getItems(itemClass: Class<out Item>, keys: Array<String>): Flow<List<Item>> {
        return localRepository.getItemsFlowable(itemClass, keys)
    }

    override fun getItemsFlowable(itemClass: Class<out Item>): Flowable<out List<Item>> {
        return localRepository.getItemsFlowable(itemClass)
    }

    override fun getItems(itemClass: Class<out Item>): Flow<List<Item>> {
        return localRepository.getItems(itemClass)
    }

    override fun getEquipment(key: String): Flowable<Equipment> {
        return localRepository.getEquipment(key)
    }

    override fun openMysteryItem(user: User?): Flowable<Equipment> {
        return apiClient.openMysteryItem()
            .flatMap { localRepository.getEquipment(it.key ?: "").firstElement().toFlowable() }
            .doOnNext { itemData ->
                val liveEquipment = localRepository.getLiveObject(itemData)
                localRepository.executeTransaction {
                    liveEquipment?.owned = true
                }
                localRepository.decrementMysteryItemCount(user)
            }
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
        return localRepository.getOwnedMounts(userID)
    }

    override fun getPets(): Flow<List<Pet>> {
        return localRepository.getPets()
    }

    override fun getPets(type: String?, group: String?, color: String?): Flow<List<Pet>> {
        return localRepository.getPets(type, group, color)
    }

    override fun getOwnedPets(): Flow<List<OwnedPet>> {
        return localRepository.getOwnedPets(userID)
    }

    override fun updateOwnedEquipment(user: User) {
        localRepository.updateOwnedEquipment(user)
    }

    override fun changeOwnedCount(type: String, key: String, amountToAdd: Int) {
        localRepository.changeOwnedCount(type, key, userID, amountToAdd)
    }

    override fun sellItem(type: String, key: String): Flowable<User> {
        return localRepository.getOwnedItem(userID, type, key, true)
            .flatMap { item -> sellItem(item) }
    }

    override fun sellItem(item: OwnedItem): Flowable<User> {
        return localRepository.getItem(item.itemType ?: "", item.key ?: "")
            .flatMap { newItem -> sellItem(newItem, item) }
    }

    override fun getLatestMysteryItem(): Flowable<Equipment> {
        return localRepository.getLatestMysteryItem()
    }

    override fun getItem(type: String, key: String): Flowable<Item> {
        return localRepository.getItem(type, key)
    }

    private fun sellItem(item: Item, ownedItem: OwnedItem): Flowable<User> {
        localRepository.executeTransaction {
            val liveItem = localRepository.getLiveObject(ownedItem)
            liveItem?.numberOwned = (liveItem?.numberOwned ?: 0) - 1
        }
        return apiClient.sellItem(item.type, item.key)
            .map { user ->
                localRepository.soldItem(userID, user)
            }
    }

    override fun equipGear(equipment: String, asCostume: Boolean): Flowable<Items> {
        return equip(if (asCostume) "costume" else "equipped", equipment)
    }

    override fun equip(type: String, key: String): Flowable<Items> {
        val liveUser = localRepository.getLiveUser(userID)

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
        return apiClient.equipItem(type, key)
            .doOnNext { items ->
                if (liveUser == null) return@doOnNext
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
            }
    }

    override fun feedPet(pet: Pet, food: Food): Flowable<FeedResponse> {
        return apiClient.feedPet(pet.key ?: "", food.key)
            .doOnNext { feedResponse ->
                localRepository.feedPet(food.key, pet.key ?: "", feedResponse.value ?: 0, userID)
            }
    }

    override fun hatchPet(egg: Egg, hatchingPotion: HatchingPotion, successFunction: () -> Unit): Flowable<Items> {
        if (appConfigManager.enableLocalChanges()) {
            localRepository.hatchPet(egg.key, hatchingPotion.key, userID)
            successFunction()
        }
        return apiClient.hatchPet(egg.key, hatchingPotion.key)
            .doOnNext {
                localRepository.save(it, userID)
                if (!appConfigManager.enableLocalChanges()) {
                    successFunction()
                }
            }
    }

    override fun inviteToQuest(quest: QuestContent): Flowable<Quest> {
        return apiClient.inviteToQuest("party", quest.key)
            .doOnNext { localRepository.changeOwnedCount("quests", quest.key, userID, -1) }
    }

    override fun buyItem(user: User?, id: String, value: Double, purchaseQuantity: Int): Flowable<BuyResponse> {
        return apiClient.buyItem(id, purchaseQuantity)
            .doOnNext { buyResponse ->
                if (user == null) {
                    return@doOnNext
                }
                val copiedUser = localRepository.getUnmanagedCopy(user)
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
                    copiedUser.stats?.gp = copiedUser.stats?.gp ?: 0 - (value * purchaseQuantity)
                }
                if (buyResponse.lvl != null) {
                    copiedUser.stats?.lvl = buyResponse.lvl
                }
                localRepository.save(copiedUser)
            }
    }

    override fun getAvailableLimitedItems(): Flowable<List<Item>> {
        return localRepository.getAvailableLimitedItems()
    }

    override fun retrieveShopInventory(identifier: String): Flowable<Shop> {
        return apiClient.retrieveShopIventory(identifier)
    }

    override fun retrieveMarketGear(): Flowable<Shop> {
        return apiClient.retrieveMarketGear()
    }

    override fun purchaseMysterySet(categoryIdentifier: String): Flowable<Void> {
        return apiClient.purchaseMysterySet(categoryIdentifier)
    }

    override fun purchaseHourglassItem(purchaseType: String, key: String): Flowable<Void> {
        return apiClient.purchaseHourglassItem(purchaseType, key)
    }

    override fun purchaseQuest(key: String): Flowable<Void> {
        return apiClient.purchaseQuest(key)
    }

    override fun purchaseSpecialSpell(key: String): Flowable<Void> {
        return apiClient.purchaseSpecialSpell(key)
    }

    override fun purchaseItem(purchaseType: String, key: String, purchaseQuantity: Int): Flowable<Void> {
        return apiClient.purchaseItem(purchaseType, key, purchaseQuantity)
    }

    override fun togglePinnedItem(item: ShopItem): Flowable<List<ShopItem>> {
        return if (!item.isValid) {
            Flowable.empty()
        } else apiClient.togglePinnedItem(item.pinType ?: "", item.path ?: "")
            .flatMap { retrieveInAppRewards() }
    }
}
