package com.habitrpg.android.habitica.data

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

interface InventoryRepository : BaseRepository {

    fun getArmoireRemainingCount(): Long

    fun getInAppRewards(): Flowable<out List<ShopItem>>
    fun getOwnedEquipment(): Flowable<out List<Equipment>>

    fun getMounts(): Flow<List<Mount>>

    fun getOwnedMounts(): Flow<List<OwnedMount>>

    fun getPets(): Flow<List<Pet>>

    fun getOwnedPets(): Flow<List<OwnedPet>>
    fun getQuestContent(key: String): Flowable<QuestContent>
    fun getQuestContent(keys: List<String>): Flow<List<QuestContent>>

    fun getEquipment(searchedKeys: List<String>): Flowable<out List<Equipment>>
    fun retrieveInAppRewards(): Flowable<List<ShopItem>>

    fun getOwnedEquipment(type: String): Flowable<out List<Equipment>>
    fun getEquipmentType(type: String, set: String): Flowable<out List<Equipment>>

    fun getOwnedItems(itemType: String, includeZero: Boolean = false): Flow<List<OwnedItem>>
    fun getOwnedItems(includeZero: Boolean = false): Flowable<Map<String, OwnedItem>>

    fun getEquipment(key: String): Flowable<Equipment>

    fun openMysteryItem(user: User?): Flowable<Equipment>

    fun saveEquipment(equipment: Equipment)
    fun getMounts(type: String?, group: String?, color: String?): Flow<List<Mount>>
    fun getPets(type: String?, group: String?, color: String?): Flow<List<Pet>>

    fun updateOwnedEquipment(user: User)

    fun changeOwnedCount(type: String, key: String, amountToAdd: Int)

    fun sellItem(type: String, key: String): Flowable<User>
    fun sellItem(item: OwnedItem): Flowable<User>

    fun equipGear(equipment: String, asCostume: Boolean): Flowable<Items>
    fun equip(type: String, key: String): Flowable<Items>

    fun feedPet(pet: Pet, food: Food): Flowable<FeedResponse>

    fun hatchPet(egg: Egg, hatchingPotion: HatchingPotion, successFunction: () -> Unit): Flowable<Items>

    fun inviteToQuest(quest: QuestContent): Flowable<Quest>

    fun buyItem(user: User?, id: String, value: Double, purchaseQuantity: Int): Flowable<BuyResponse>

    fun retrieveShopInventory(identifier: String): Flowable<Shop>
    fun retrieveMarketGear(): Flowable<Shop>

    fun purchaseMysterySet(categoryIdentifier: String): Flowable<Void>

    fun purchaseHourglassItem(purchaseType: String, key: String): Flowable<Void>

    fun purchaseQuest(key: String): Flowable<Void>
    fun purchaseSpecialSpell(key: String): Flowable<Void>

    fun purchaseItem(purchaseType: String, key: String, purchaseQuantity: Int): Flowable<Void>

    fun togglePinnedItem(item: ShopItem): Flowable<List<ShopItem>>
    fun getItemsFlowable(itemClass: Class<out Item>, keys: Array<String>): Flow<List<Item>>
    fun getItemsFlowable(itemClass: Class<out Item>): Flowable<out List<Item>>
    fun getItems(itemClass: Class<out Item>): Flow<List<Item>>
    fun getLatestMysteryItem(): Flowable<Equipment>
    fun getItem(type: String, key: String): Flowable<Item>
    fun getAvailableLimitedItems(): Flowable<List<Item>>
}
