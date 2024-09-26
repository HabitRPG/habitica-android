package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.inventory.EquipmentSet
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
import kotlinx.coroutines.flow.Flow

interface InventoryLocalRepository : ContentLocalRepository {
    fun getArmoireRemainingCount(): Flow<Int>

    fun getOwnedEquipment(): Flow<List<Equipment>>

    fun getMounts(): Flow<List<Mount>>

    fun getOwnedMounts(userID: String): Flow<List<OwnedMount>>

    fun getPets(): Flow<List<Pet>>

    fun getOwnedPets(userID: String): Flow<List<OwnedPet>>

    fun getInAppRewards(): Flow<List<ShopItem>>

    fun getInAppReward(key: String): Flow<ShopItem>

    fun getQuestContent(key: String): Flow<QuestContent?>

    fun getQuestContent(keys: List<String>): Flow<List<QuestContent>>

    fun getEquipment(searchedKeys: List<String>): Flow<List<Equipment>>

    fun getOwnedEquipment(type: String): Flow<List<Equipment>>

    fun getOwnedItems(
        itemType: String,
        userID: String,
        includeZero: Boolean,
    ): Flow<List<OwnedItem>>

    fun getOwnedItems(
        userID: String,
        includeZero: Boolean,
    ): Flow<Map<String, OwnedItem>>

    fun getEquipmentType(
        type: String,
        set: String,
    ): Flow<List<Equipment>>

    fun getEquipment(key: String): Flow<Equipment>

    fun getMounts(
        type: String?,
        group: String?,
        color: String?,
    ): Flow<List<Mount>>

    fun getPets(
        type: String?,
        group: String?,
        color: String?,
    ): Flow<List<Pet>>

    fun updateOwnedEquipment(user: User)

    suspend fun changeOwnedCount(
        type: String,
        key: String,
        userID: String,
        amountToAdd: Int,
    )

    fun changeOwnedCount(
        item: OwnedItem,
        amountToAdd: Int?,
    )

    fun getItem(
        type: String,
        key: String,
    ): Flow<Item>

    fun getOwnedItem(
        userID: String,
        type: String,
        key: String,
        includeZero: Boolean,
    ): Flow<OwnedItem>

    fun decrementMysteryItemCount(user: User?)

    fun saveInAppRewards(onlineItems: List<ShopItem>)

    fun hatchPet(
        eggKey: String,
        potionKey: String,
        userID: String,
    )

    fun unhatchPet(
        eggKey: String,
        potionKey: String,
        userID: String,
    )

    fun feedPet(
        foodKey: String,
        petKey: String,
        feedValue: Int,
        userID: String,
    )

    fun getLatestMysteryItem(): Flow<Equipment>
    fun getLatestMysteryItemAndSet(): Flow<Pair<Equipment, EquipmentSet?>>

    fun soldItem(
        userID: String,
        updatedUser: User,
    ): User

    fun getAvailableLimitedItems(): Flow<List<Item>>

    fun save(
        items: Items,
        userID: String,
    )

    fun getLiveObject(obj: OwnedItem): OwnedItem?

    fun getItems(itemClass: Class<out Item>): Flow<List<Item>>

    fun getItems(
        itemClass: Class<out Item>,
        keys: Array<String>,
    ): Flow<List<Item>>
}
