package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.inventory.Item
import com.habitrpg.android.habitica.models.inventory.Mount
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.user.Items
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.OwnedMount
import com.habitrpg.android.habitica.models.user.OwnedPet
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.rxjava3.core.Flowable
import kotlinx.coroutines.flow.Flow

interface InventoryLocalRepository : ContentLocalRepository {

    fun getArmoireRemainingCount(): Long
    fun getOwnedEquipment(): Flowable<out List<Equipment>>

    fun getMounts(): Flow<List<Mount>>

    fun getOwnedMounts(userID: String): Flow<List<OwnedMount>>

    fun getPets(): Flow<List<Pet>>

    fun getOwnedPets(userID: String): Flow<List<OwnedPet>>

    fun getInAppRewards(): Flowable<out List<ShopItem>>
    fun getQuestContent(key: String): Flowable<QuestContent>
    fun getQuestContent(keys: List<String>): Flow<List<QuestContent>>

    fun getEquipment(searchedKeys: List<String>): Flowable<out List<Equipment>>

    fun getOwnedEquipment(type: String): Flowable<out List<Equipment>>

    fun getItems(itemClass: Class<out Item>, keys: Array<String>): Flow<List<Item>>
    fun getItems(itemClass: Class<out Item>): Flowable<out List<Item>>
    fun getOwnedItems(itemType: String, userID: String, includeZero: Boolean): Flow<List<OwnedItem>>
    fun getOwnedItems(userID: String, includeZero: Boolean): Flowable<Map<String, OwnedItem>>
    fun getEquipmentType(type: String, set: String): Flowable<out List<Equipment>>

    fun getEquipment(key: String): Flowable<Equipment>
    fun getMounts(type: String?, group: String?, color: String?): Flow<List<Mount>>
    fun getPets(type: String?, group: String?, color: String?): Flow<List<Pet>>

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

    fun getLiveObject(obj: OwnedItem): OwnedItem?
}
