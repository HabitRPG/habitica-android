package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.InventoryLocalRepository
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.inventory.EquipmentSet
import com.habitrpg.android.habitica.models.inventory.Food
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.models.inventory.Item
import com.habitrpg.android.habitica.models.inventory.Mount
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.inventory.SpecialItem
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.user.Items
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.OwnedMount
import com.habitrpg.android.habitica.models.user.OwnedPet
import com.habitrpg.android.habitica.models.user.User
import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RealmInventoryLocalRepository(
    realm: Realm,
) : RealmContentLocalRepository(realm),
    InventoryLocalRepository {
    override fun getQuestContent(keys: List<String>): Flow<List<QuestContent>> = safeFindAll {
        it.where(QuestContent::class.java).`in`("key", keys.toTypedArray())
    }

    override fun getQuestContent(key: String): Flow<QuestContent?> = safeFindOne {
        it.where(QuestContent::class.java).equalTo("key", key)
    }

    override fun getEquipment(searchedKeys: List<String>): Flow<List<Equipment>> = safeFindAll {
        it.where(Equipment::class.java).`in`("key", searchedKeys.toTypedArray())
    }

    override fun getArmoireRemainingCount(): Flow<Int> =
        safeFindAll {
            it.where(Equipment::class.java)
                .equalTo("klass", "armoire")
                .beginGroup()
                .equalTo("owned", false)
                .or()
                .isNull("owned")
                .endGroup()
        }.map { it.count() }

    override fun getOwnedEquipment(type: String): Flow<List<Equipment>> = safeFindAll {
        it.where(Equipment::class.java)
            .equalTo("type", type)
            .equalTo("owned", true)
    }

    override fun getOwnedEquipment(): Flow<List<Equipment>> = safeFindAll {
        it.where(Equipment::class.java).equalTo("owned", true)
    }

    override fun getEquipmentType(
        type: String,
        set: String,
    ): Flow<List<Equipment>> = safeFindAll {
        it.where(Equipment::class.java)
            .equalTo("type", type)
            .equalTo("gearSet", set)
    }

    override fun incrementGemsBought(currentUserID: String, purchaseQuantity: Int) {
        val user = getLiveUser(currentUserID) ?: return
        executeTransaction {
            user.purchased?.plan?.gemsBought = purchaseQuantity + (user.purchased?.plan?.gemsBought ?: 0)
        }
    }

    override fun setOwnedCount(
        ownedItem: OwnedItem,
        newCount: Int
    ) {
        val liveItem = getLiveObject(ownedItem)
        executeTransaction {
            liveItem?.numberOwned = newCount
        }
    }

    override fun markAsOwned(
        equipment: Equipment,
        isOwned: Boolean
    ) {
        val liveEquipment = getLiveObject(equipment)
        executeTransaction {
            liveEquipment?.owned = isOwned
        }
    }

    override fun getOwnedItems(
        itemType: String,
        userID: String,
        includeZero: Boolean,
    ): Flow<List<OwnedItem>> =
        queryUser(userID).map {
            val items =
                when (itemType) {
                    "eggs" -> it?.items?.eggs
                    "hatchingPotions" -> it?.items?.hatchingPotions
                    "food" -> it?.items?.food
                    "quests" -> it?.items?.quests
                    "special" -> it?.items?.special
                    else -> emptyList()
                } ?: emptyList()
            if (includeZero) {
                items
            } else {
                items.filter { it.numberOwned > 0 }
            }
        }

    override fun getItems(itemClass: Class<out Item>): Flow<List<Item>> = safeFindAll {
        it.where(itemClass)
    }

    override fun getItems(
        itemClass: Class<out Item>,
        keys: Array<String>,
    ): Flow<List<Item>> = safeFindAll {
        it.where(itemClass).`in`("key", keys)
    }

    override fun getOwnedItems(
        userID: String,
        includeZero: Boolean,
    ): Flow<Map<String, OwnedItem>> =
        queryUser(userID)
            .filterNotNull()
            .map { user ->
                val items = HashMap<String, OwnedItem>()
                user.items?.eggs?.forEach { items[it.key + "-" + it.itemType] = it }
                user.items?.food?.forEach { items[it.key + "-" + it.itemType] = it }
                user.items?.hatchingPotions?.forEach { items[it.key + "-" + it.itemType] = it }
                user.items?.quests?.forEach { items[it.key + "-" + it.itemType] = it }
                user.items?.special?.forEach { items[it.key + "-" + it.itemType] = it }
                if (includeZero) {
                    items
                } else {
                    items.filter { it.value.numberOwned > 0 }
                }
            }

    override fun getEquipment(key: String): Flow<Equipment> = safeFindOne {
        it.where(Equipment::class.java).equalTo("key", key)
    }

    override fun getMounts(): Flow<List<Mount>> = safeFindAll {
        it.where(Mount::class.java).sort("type", Sort.ASCENDING, "animal", Sort.ASCENDING)
    }

    override fun getMounts(
        type: String?,
        group: String?,
        color: String?,
    ): Flow<List<Mount>> = safeFindAll { realm ->
        var query =
            realm
                .where(Mount::class.java)
                .sort("type", Sort.ASCENDING, if (color == null) "color" else "animal", Sort.ASCENDING)
        if (type != null) {
            query = query.equalTo("animal", type)
        }
        if (group != null) {
            query = query.equalTo("type", group)
        }
        if (color != null) {
            query = query.equalTo("color", color)
        }
        query
    }

    override fun getOwnedMounts(userID: String): Flow<List<OwnedMount>> =
        queryUser(userID)
            .map { user ->
                user?.items?.mounts?.filter {
                    it.owned
                } ?: emptyList()
            }

    override fun getPets(): Flow<List<Pet>> = safeFindAll {
        it.where(Pet::class.java).sort("type", Sort.ASCENDING, "animal", Sort.ASCENDING)
    }

    override fun getPets(
        type: String?,
        group: String?,
        color: String?,
    ): Flow<List<Pet>> = safeFindAll { realm ->
        var query =
            realm
                .where(Pet::class.java)
                .sort("type", Sort.ASCENDING, if (color == null) "color" else "animal", Sort.ASCENDING)
        if (type != null) {
            query = query.equalTo("animal", type)
        }
        if (group != null) {
            query = query.equalTo("type", group)
        }
        if (color != null) {
            query = query.equalTo("color", color)
        }
        query
    }

    override fun getOwnedPets(userID: String): Flow<List<OwnedPet>> = queryUser(userID)
            .map {
                it.items?.pets?.filter {
                    it.trained > 0
                } ?: emptyList()
            }

    override fun updateOwnedEquipment(user: User) {
    }

    override suspend fun changeOwnedCount(
        type: String,
        key: String,
        userID: String,
        amountToAdd: Int,
    ) {
        val item = getOwnedItem(userID, type, key, true).firstOrNull()
        if (item != null) {
            changeOwnedCount(item, amountToAdd)
        }
    }

    override fun changeOwnedCount(
        item: OwnedItem,
        amountToAdd: Int?,
    ) {
        val liveItem = getLiveObject(item) ?: return
        amountToAdd?.let { amount ->
            executeTransaction { liveItem.numberOwned += amount }
        }
    }

    override fun getOwnedItem(
        userID: String,
        type: String,
        key: String,
        includeZero: Boolean,
    ): Flow<OwnedItem> =
        queryUser(userID)
            .map {
                var items = (
                    when (type) {
                        "eggs" -> it.items?.eggs
                        "hatchingPotions" -> it.items?.hatchingPotions
                        "food" -> it.items?.food
                        "quests" -> it.items?.quests
                        else -> emptyList()
                    } ?: emptyList()
                )
                items = items.filter { it.key == key }
                if (includeZero) {
                    items
                } else {
                    items.filter { it.numberOwned > 0 }
                }
            }.filter { it.isNotEmpty() }
            .map { it.first() }

    override fun getItem(
        type: String,
        key: String,
    ): Flow<Item> {
        val itemClass: Class<out RealmObject> =
            when (type) {
                "eggs" -> Egg::class.java
                "hatchingPotions" -> HatchingPotion::class.java
                "food" -> Food::class.java
                "quests" -> QuestContent::class.java
                "special" -> SpecialItem::class.java
                else -> Egg::class.java
            }
        return safeFindOne { it.where(itemClass).equalTo("key", key) }
            .mapNotNull { it as? Item }
    }

    override fun decrementMysteryItemCount(user: User?) {
        if (user == null) {
            return
        }
        val liveUser = getLiveObject(user)
        val ownedItems = liveUser?.items?.special
        val item = ownedItems?.firstOrNull { it.key == "inventory_present" }
        executeTransaction {
            if (item != null && item.isValid) {
                item.numberOwned = item.numberOwned - 1
            }
            if (liveUser?.isValid == true) {
                liveUser.purchased?.plan?.mysteryItemCount =
                    (user.purchased?.plan?.mysteryItemCount ?: 0) - 1
            }
        }
    }

    override fun getInAppRewards(): Flow<List<ShopItem>> = safeFindAll {
        it.where(ShopItem::class.java)
    }

    override fun getInAppReward(key: String): Flow<ShopItem> = safeFindOne {
        it.where(ShopItem::class.java).equalTo("key", key)
    }

    override fun saveInAppRewards(onlineItems: List<ShopItem>) {
        val localItems =
            safeQuery { it.where(ShopItem::class.java) }?.findAll()?.createSnapshot() ?: return
        executeTransaction {
            for (localItem in localItems) {
                if (!onlineItems.contains(localItem)) {
                    localItem.deleteFromRealm()
                }
            }
            realm.insertOrUpdate(onlineItems)
        }
    }

    override fun hatchPet(
        eggKey: String,
        potionKey: String,
        userID: String,
    ) {
        val newPet = OwnedPet()
        newPet.key = "$eggKey-$potionKey"
        newPet.trained = 5
        val user = safeQuery { it.where(User::class.java).equalTo("id", userID) }?.findFirst() ?: return
        val egg = user.items?.eggs?.firstOrNull { it.key == eggKey } ?: return
        val hatchingPotion =
            user.items?.hatchingPotions?.firstOrNull { it.key == potionKey } ?: return
        executeTransaction {
            egg.numberOwned -= 1
            hatchingPotion.numberOwned -= 1
            user.items?.pets?.add(newPet)
        }
    }

    override fun getLiveObject(obj: OwnedItem): OwnedItem? {
        if (!obj.isManaged) return obj
        return safeQuery {
            it.where(OwnedItem::class.java)
                .equalTo("key", obj.key)
                .equalTo("itemType", obj.itemType)
        }?.findFirst()
    }

    override fun save(
        items: Items,
        userID: String,
    ) {
        val user = safeQuery { it.where(User::class.java).equalTo("id", userID) }?.findFirst() ?: return
        items.setItemTypes()
        executeTransaction {
            user.items = items
        }
    }

    override fun unhatchPet(
        eggKey: String,
        potionKey: String,
        userID: String,
    ) {
        val pet = safeQuery { it.where(OwnedPet::class.java).equalTo("key", "$eggKey-$potionKey") }?.findFirst()
        val user = safeQuery { it.where(User::class.java).equalTo("id", userID) }?.findFirst() ?: return
        val egg = user.items?.eggs?.firstOrNull { it.key == eggKey } ?: return
        val hatchingPotion =
            user.items?.hatchingPotions?.firstOrNull { it.key == potionKey } ?: return
        executeTransaction {
            egg.numberOwned += 1
            hatchingPotion.numberOwned += 1
            user.items?.pets?.remove(pet)
        }
    }

    override fun feedPet(
        foodKey: String,
        petKey: String,
        feedValue: Int,
        userID: String,
    ) {
        val user = safeQuery { it.where(User::class.java).equalTo("id", userID) }?.findFirst() ?: return
        val pet = user.items?.pets?.firstOrNull { it.key == petKey } ?: return
        val food = user.items?.food?.firstOrNull { it.key == foodKey } ?: return
        executeTransaction {
            pet.trained = feedValue
            food.numberOwned -= 1

            if (feedValue < 0) {
                val mount = OwnedMount()
                mount.key = petKey
                mount.owned = true
                user.items?.mounts?.add(mount)
            }
        }
    }

    override fun getLatestMysteryItem(): Flow<Equipment> =
        safeFindAll {
            it.where(Equipment::class.java)
                .contains("key", "mystery_2")
                .sort("mystery", Sort.DESCENDING)
        }.map {
            val format = SimpleDateFormat("yyyyMM", Locale.US)
            it.first { equipment ->
                equipment.key?.contains(format.format(Date())) == true
            }
        }

    private fun getLatestMysterySet(): Flow<EquipmentSet?> =
        safeFindAll {
            it.where(EquipmentSet::class.java).sort("key", Sort.DESCENDING)
        }.map {
            val dateString = SimpleDateFormat("yyyyMM", Locale.US).format(Date())
            it.firstOrNull { set ->
                set.key.contains(dateString)
            }
        }

    override fun getLatestMysteryItemAndSet(): Flow<Pair<Equipment, EquipmentSet?>> =
        getLatestMysteryItem().combine(getLatestMysterySet()) { item, set ->
            Pair(item, set)
        }

    override fun soldItem(
        userID: String,
        updatedUser: User,
    ): User {
        val user =
            safeQuery { it.where(User::class.java).equalTo("id", userID) }
                ?.findFirst() ?: return updatedUser
        executeTransaction {
            val items = updatedUser.items
            if (items != null) {
                user.items = items
            }
            val stats = updatedUser.stats
            if (stats != null) {
                user.stats?.apply {
                    hp = stats.hp
                    mp = stats.mp
                    exp = stats.exp
                    gp = stats.gp
                }
            }
        }
        return user
    }

    private fun <T : RealmModel> queryLimitedItems(clazz: Class<T>): Flow<List<T>> = safeFindAll {
        it.where(clazz)
            .lessThan("event.start", Date())
            .greaterThan("event.end", Date())
    }

    override fun getAvailableLimitedItems(): Flow<List<Item>> =
        queryLimitedItems(Egg::class.java)
            .combine(
                queryLimitedItems(Food::class.java)
            ) { items, food ->
                items + food
            }.combine(
                queryLimitedItems(HatchingPotion::class.java)
            ) { items, hatchingPotions ->
                items + hatchingPotions
            }.combine(
                queryLimitedItems(QuestContent::class.java)
            ) { items, questContent ->
                items + questContent
            }
            .filter { it.firstOrNull()?.isValid == true }
}
