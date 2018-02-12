package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.local.InventoryLocalRepository
import com.habitrpg.android.habitica.helpers.RemoteConfigManager
import com.habitrpg.android.habitica.models.inventory.*
import com.habitrpg.android.habitica.models.responses.BuyResponse
import com.habitrpg.android.habitica.models.responses.FeedResponse
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.user.Items
import com.habitrpg.android.habitica.models.user.User
import io.realm.RealmResults
import rx.Observable
import java.util.*

class InventoryRepositoryImpl(localRepository: InventoryLocalRepository, apiClient: ApiClient, private val remoteConfigManager: RemoteConfigManager) : ContentRepositoryImpl<InventoryLocalRepository>(localRepository, apiClient), InventoryRepository {

    override fun getQuestContent(key: String): Observable<QuestContent> {
        return localRepository.getQuestContent(key)
    }

    override fun getItems(searchedKeys: List<String>): Observable<RealmResults<Equipment>> {
        return localRepository.getEquipment(searchedKeys)
    }

    override fun getArmoireRemainingCount(): Long {
        return localRepository.getArmoireRemainingCount()
    }

    override fun getInAppRewards(): Observable<RealmResults<ShopItem>> {
        return localRepository.getInAppRewards()
    }

    override fun retrieveInAppRewards(): Observable<List<ShopItem>> {
        return if (remoteConfigManager.newShopsEnabled()!!) {
            apiClient.retrieveInAppRewards()
                    .doOnNext({ localRepository.saveInAppRewards(it) })
        } else {
            apiClient.retrieveOldGear()
                    .map<List<String>> { items ->
                        val itemKeys = ArrayList<String>()
                        for (item in items) {
                            itemKeys.add(item.key)
                        }
                        itemKeys.add("potion")
                        itemKeys.add("armoire")
                        itemKeys
                    }
                    .flatMap<RealmResults<Equipment>>({ this.getItems(it) })
                    .map<List<ShopItem>> { items ->
                        val buyableItems = ArrayList<ShopItem>()
                        if (items != null) {
                            for (item in items) {
                                val shopItem = ShopItem()
                                shopItem.key = item.key ?: ""
                                shopItem.text = item.text
                                shopItem.notes = item.notes
                                shopItem.value = item.value.toInt()
                                shopItem.currency = "gold"
                                if ("potion" == item.key) {
                                    shopItem.purchaseType = "potion"
                                } else if ("armoire" == item.key) {
                                    shopItem.purchaseType = "armoire"
                                } else {
                                    shopItem.purchaseType = "gear"
                                }

                                buyableItems.add(shopItem)
                            }
                        }
                        buyableItems
                    }
                    .doOnNext({ localRepository.saveInAppRewards(it) })
                    .first()
        }
    }

    override fun getOwnedEquipment(type: String): Observable<RealmResults<Equipment>> {
        return localRepository.getOwnedEquipment(type)
    }

    override fun getOwnedEquipment(): Observable<RealmResults<Equipment>> {
        return localRepository.getOwnedEquipment()
    }

    override fun getOwnedItems(itemClass: Class<out Item>, user: User?): Observable<out RealmResults<out Item>> {
        return localRepository.getOwnedItems(itemClass, user)
    }

    override fun getOwnedItems(user: User): Observable<out Map<String, Item>> {
        return localRepository.getOwnedItems(user)
    }

    override fun getEquipment(key: String): Observable<Equipment> {
        return localRepository.getEquipment(key)
    }

    override fun openMysteryItem(user: User): Observable<Equipment> {
        return apiClient.openMysteryItem().doOnNext { itemData ->
            itemData.owned = true
            localRepository.save(itemData)
            localRepository.decrementMysteryItemCount(user)
        }
    }

    override fun saveEquipment(equipment: Equipment) {
        localRepository.save(equipment)
    }

    override fun getMounts(): Observable<RealmResults<Mount>> {
        return localRepository.getMounts()
    }

    override fun getMounts(type: String, group: String): Observable<RealmResults<Mount>> {
        return localRepository.getMounts(type, group)
    }

    override fun getOwnedMounts(): Observable<RealmResults<Mount>> {
        return localRepository.getOwnedMounts()
    }

    override fun getOwnedMounts(animalType: String, animalGroup: String): Observable<RealmResults<Mount>> {
        return localRepository.getOwnedMounts(animalType, animalGroup)
    }

    override fun getPets(): Observable<RealmResults<Pet>> {
        return localRepository.getPets()
    }

    override fun getPets(type: String, group: String): Observable<RealmResults<Pet>> {
        return localRepository.getPets(type, group)
    }

    override fun getOwnedPets(): Observable<RealmResults<Pet>> {
        return localRepository.getOwnedPets()
    }

    override fun getOwnedPets(type: String, group: String): Observable<RealmResults<Pet>> {
        return localRepository.getOwnedPets(type, group)
    }

    override fun updateOwnedEquipment(user: User) {
        localRepository.updateOwnedEquipment(user)
    }

    override fun changeOwnedCount(type: String, key: String, amountToAdd: Int) {
        localRepository.changeOwnedCount(type, key, amountToAdd)
    }

    override fun sellItem(user: User, type: String, key: String): Observable<User> {
        return localRepository.getItem(type, key)
                .flatMap { item -> sellItem(user, item) }
    }

    override fun sellItem(user: User?, item: Item): Observable<User> {
        return apiClient.sellItem(item.type, item.key)
                .map { user1 ->
                    localRepository.executeTransaction { realm ->
                        if (user != null) {
                            if (user1.items != null) {
                                user1.items.userId = user.id
                                val items = realm.copyToRealmOrUpdate(user1.items)
                                user.items = items
                            } else {
                                item.owned = item.owned - 1
                            }
                            if (user.stats != null) {
                                user1.stats.userId = user.id
                                val stats = realm.copyToRealmOrUpdate(user1.stats)
                                user.stats = stats
                            }
                        }
                    }
                    user
                }
    }

    override fun equipGear(user: User, key: String, asCostume: Boolean): Observable<Items> {
        return equip(user, if (asCostume) "costume" else "equipped", key)
    }

    override fun equip(user: User?, type: String, key: String): Observable<Items> {
        return apiClient.equipItem(type, key)
                .doOnNext { items ->
                    if (user == null) {
                        return@doOnNext
                    }
                    localRepository.executeTransaction {
                        val newEquipped = items.gear.equipped
                        val oldEquipped = user.items.gear.equipped
                        val newCostume = items.gear.costume
                        val oldCostume = user.items.gear.costume
                        oldEquipped.updateWith(newEquipped)
                        oldCostume.updateWith(newCostume)
                        user.items.currentMount = items.currentMount
                        user.items.currentPet = items.currentPet
                        user.balance = user.balance
                    }
                }
    }

    override fun feedPet(pet: Pet, food: Food): Observable<FeedResponse> {
        return apiClient.feedPet(pet.key, food.key)
                .doOnNext { feedResponse ->
                    localRepository.changeOwnedCount(food, -1)
                    localRepository.executeTransaction { pet.trained = feedResponse.value }
                }
    }

    override fun hatchPet(egg: Egg, hatchingPotion: HatchingPotion): Observable<Items> {
        return apiClient.hatchPet(egg.key, hatchingPotion.key)
                .doOnNext {
                    localRepository.changeOwnedCount(egg, -1)
                    localRepository.changeOwnedCount(hatchingPotion, -1)
                    localRepository.changePetFeedStatus(egg.key+"-"+hatchingPotion.key, 5)
                }
    }

    override fun inviteToQuest(quest: QuestContent): Observable<Quest> {
        return apiClient.inviteToQuest("party", quest.key)
                .doOnNext { localRepository.changeOwnedCount(quest, -1) }
    }

    override fun buyItem(user: User, key: String, value: Double): Observable<BuyResponse> {
        return apiClient.buyItem(key)
                .doOnNext { buyResponse ->
                    val copiedUser = localRepository.getUnmanagedCopy(user)
                    if (buyResponse.items != null) {
                        buyResponse.items.userId = user.id
                        copiedUser.items = buyResponse.items
                    }
                    if (buyResponse.hp != null) {
                        copiedUser.stats.setHp(buyResponse.hp)
                    }
                    if (buyResponse.exp != null) {
                        copiedUser.stats.setExp(buyResponse.exp)
                    }
                    if (buyResponse.mp != null) {
                        copiedUser.stats.setMp(buyResponse.mp)
                    }
                    if (buyResponse.gp != null) {
                        copiedUser.stats.setGp(buyResponse.gp)
                    } else {
                        copiedUser.stats.setGp(copiedUser.stats.getGp()!! - value)
                    }
                    if (buyResponse.lvl != null) {
                        copiedUser.stats.setLvl(buyResponse.lvl)
                    }
                    localRepository.save(copiedUser)
                }
    }

    override fun retrieveShopInventory(identifier: String): Observable<Shop> {
        return apiClient.retrieveShopIventory(identifier)
    }

    override fun retrieveMarketGear(): Observable<Shop> {
        return apiClient.retrieveMarketGear()
    }

    override fun purchaseMysterySet(categoryIdentifier: String): Observable<Void> {
        return apiClient.purchaseMysterySet(categoryIdentifier)
    }

    override fun purchaseHourglassItem(purchaseType: String, key: String): Observable<Void> {
        return apiClient.purchaseHourglassItem(purchaseType, key)
    }

    override fun purchaseQuest(key: String): Observable<Void> {
        return apiClient.purchaseQuest(key)
    }

    override fun purchaseItem(purchaseType: String, key: String): Observable<Void> {
        return apiClient.purchaseItem(purchaseType, key)
    }

    override fun togglePinnedItem(item: ShopItem): Observable<List<ShopItem>> {
        return if (!item.isValid) {
            Observable.just(null)
        } else apiClient.togglePinnedItem(item.pinType ?: "", item.path ?: "")
                .flatMap { retrieveInAppRewards() }
    }
}
