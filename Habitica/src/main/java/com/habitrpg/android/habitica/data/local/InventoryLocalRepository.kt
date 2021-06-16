package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.inventory.*
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.user.*
import io.reactivex.rxjava3.core.Flowable

interface InventoryLocalRepository : ContentLocalRepository {

    fun getArmoireRemainingCount(): Long
    fun getOwnedEquipment(): Flowable<out List<Equipment>>

    fun getMounts(): Flowable<out List<Mount>>

    fun getOwnedMounts(userID: String): Flowable<out List<OwnedMount>>

    fun getPets(): Flowable<out List<Pet>>

    fun getOwnedPets(userID: String): Flowable<out List<OwnedPet>>

    fun getInAppRewards(): Flowable<out List<ShopItem>>
    fun getQuestContent(key: String): Flowable<QuestContent>
    fun getQuestContent(keys: List<String>): Flowable<out List<QuestContent>>

    fun getEquipment(searchedKeys: List<String>): Flowable<out List<Equipment>>

    fun getOwnedEquipment(type: String): Flowable<out List<Equipment>>

    fun getItems(itemClass: Class<out Item>, keys: Array<String>): Flowable<out List<Item>>
    fun getItems(itemClass: Class<out Item>): Flowable<out List<Item>>
    fun getOwnedItems(itemType: String, userID: String, includeZero: Boolean): Flowable<out List<OwnedItem>>
    fun getOwnedItems(userID: String, includeZero: Boolean): Flowable<Map<String, OwnedItem>>
    fun getEquipmentType(type: String, set: String): Flowable<out List<Equipment>>

    fun getEquipment(key: String): Flowable<Equipment>
    fun getMounts(type: String?, group: String?, color: String?): Flowable<out List<Mount>>
    fun getPets(type: String?, group: String?, color: String?): Flowable<out List<Pet>>

    fun updateOwnedEquipment(user: User)

    fun changeOwnedCount(type: String, key: String, userID: String, amountToAdd: Int)
    fun changeOwnedCount(item: OwnedItem, amountToAdd: Int?)

    fun getItem(type: String, key: String): Flowable<Item>
    fun getOwnedItem(userID: String, type: String, key: String, includeZero: Boolean): Flowable<OwnedItem>

    fun decrementMysteryItemCount(user: User?)
    fun saveInAppRewards(onlineItems: List<ShopItem>)

    fun hatchPet(eggKey: String, potionKey: String, userID: String)
    fun unhatchPet(eggKey: String, potionKey: String, userID: String)
    fun feedPet(foodKey: String, petKey: String, feedValue: Int, userID: String)
    fun getLatestMysteryItem(): Flowable<Equipment>
    fun soldItem(userID: String, updatedUser: User): User
    fun getAvailableLimitedItems(): Flowable<List<Item>>

    fun save(items: Items, userID: String)
}
