package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.local.InventoryLocalRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.models.inventory.*
import com.habitrpg.android.habitica.models.responses.BuyResponse
import com.habitrpg.android.habitica.models.responses.FeedResponse
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.user.*
import io.reactivex.rxjava3.core.Flowable
import io.realm.RealmResults

class InventoryRepositoryImpl(localRepository: InventoryLocalRepository, apiClient: ApiClient, userID: String, var appConfigManager: AppConfigManager) : BaseRepositoryImpl<InventoryLocalRepository>(localRepository, apiClient, userID), InventoryRepository {
    override fun getQuestContent(keys: List<String>): Flowable<RealmResults<QuestContent>> {
        return localRepository.getQuestContent(keys)
    }

    override fun getQuestContent(key: String): Flowable<QuestContent> {
        return localRepository.getQuestContent(key)
    }

    override fun getEquipment(searchedKeys: List<String>): Flowable<RealmResults<Equipment>> {
        return localRepository.getEquipment(searchedKeys)
    }

    override fun getArmoireRemainingCount(): Long {
        return localRepository.getArmoireRemainingCount()
    }

    override fun getInAppRewards(): Flowable<RealmResults<ShopItem>> {
        return localRepository.getInAppRewards()
    }

    override fun retrieveInAppRewards(): Flowable<List<ShopItem>> {
        return apiClient.retrieveInAppRewards().doOnNext { localRepository.saveInAppRewards(it) }
    }

    override fun getOwnedEquipment(type: String): Flowable<RealmResults<Equipment>> {
        return localRepository.getOwnedEquipment(type)
    }

    override fun getOwnedEquipment(): Flowable<RealmResults<Equipment>> {
        return localRepository.getOwnedEquipment()
    }

    override fun getEquipmentType(type: String, set: String): Flowable<RealmResults<Equipment>> {
        return localRepository.getEquipmentType(type, set)
    }

    override fun getOwnedItems(itemType: String, includeZero: Boolean): Flowable<RealmResults<OwnedItem>> {
        return localRepository.getOwnedItems(itemType, userID, includeZero)
    }

    override fun getOwnedItems(includeZero: Boolean): Flowable<Map<String, OwnedItem>> {
        return localRepository.getOwnedItems(userID, includeZero)
    }

    override fun getItems(itemClass: Class<out Item>, keys: Array<String>): Flowable<out RealmResults<out Item>> {
        return localRepository.getItems(itemClass, keys)
    }

    override fun getItems(itemClass: Class<out Item>): Flowable<out RealmResults<out Item>> {
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

    override fun getMounts(): Flowable<RealmResults<Mount>> {
        return localRepository.getMounts()
    }

    override fun getMounts(type: String?, group: String?, color: String?): Flowable<RealmResults<Mount>> {
        return localRepository.getMounts(type, group, color)
    }

    override fun getOwnedMounts(): Flowable<RealmResults<OwnedMount>> {
        return localRepository.getOwnedMounts(userID)
    }

    override fun getPets(): Flowable<RealmResults<Pet>> {
        return localRepository.getPets()
    }

    override fun getPets(type: String?, group: String?, color: String?): Flowable<RealmResults<Pet>> {
        return localRepository.getPets(type, group, color)
    }

    override fun getOwnedPets(): Flowable<RealmResults<OwnedPet>> {
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
        if (appConfigManager.enableLocalChanges()) {
            localRepository.executeTransaction {
                val liveItem = localRepository.getLiveObject(ownedItem)
                liveItem?.numberOwned = (liveItem?.numberOwned ?: 0) - 1
            }
        }
        return apiClient.sellItem(item.type, item.key)
                .map { user ->
                    localRepository.soldItem(userID, user)
                }
    }

    override fun equipGear(user: User?, equipment: String, asCostume: Boolean): Flowable<Items> {
        return equip(user, if (asCostume) "costume" else "equipped", equipment)
    }

    override fun equip(user: User?, type: String, key: String): Flowable<Items> {
        if (user != null && appConfigManager.enableLocalChanges()) {
            localRepository.modify(user) { liveUser ->
                if (type == "mount") {
                    liveUser.items?.currentMount = key
                } else if (type == "pet") {
                    liveUser.items?.currentPet = key
                }
                val outfit = if (type == "costume") {
                    liveUser.items?.gear?.costume
                } else {
                    liveUser.items?.gear?.equipped
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
                    if (user == null) {
                        return@doOnNext
                    }
                    localRepository.modify(user) { liveUser ->
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
                    it.userId = userID
                    localRepository.save(it)
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
                        buyResponse.items.userId = user.id
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
