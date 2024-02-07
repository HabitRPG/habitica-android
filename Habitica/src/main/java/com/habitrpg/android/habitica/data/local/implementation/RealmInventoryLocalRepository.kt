package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.InventoryLocalRepository
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.Equipment
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
import io.realm.RealmObject
import io.realm.Sort
import io.realm.kotlin.toFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RealmInventoryLocalRepository(realm: Realm) :
    RealmContentLocalRepository(realm),
    InventoryLocalRepository {
    override fun getQuestContent(keys: List<String>): Flow<List<QuestContent>> {
        return realm.where(QuestContent::class.java)
            .`in`("key", keys.toTypedArray())
            .findAll()
            .toFlow()
            .filter { it.isLoaded }
    }

    override fun getQuestContent(key: String): Flow<QuestContent?> {
        return realm.where(QuestContent::class.java).equalTo("key", key)
            .findAll()
            .toFlow()
            .filter { content -> content.isLoaded && content.isValid && !content.isEmpty() }
            .map { content -> content.first() }
    }

    override fun getEquipment(searchedKeys: List<String>): Flow<out List<Equipment>> {
        return realm.where(Equipment::class.java)
            .`in`("key", searchedKeys.toTypedArray())
            .findAll()
            .toFlow()
            .filter { it.isLoaded }
    }

    override fun getArmoireRemainingCount(): Flow<Int> {
        return realm.where(Equipment::class.java)
            .equalTo("klass", "armoire")
            .beginGroup()
            .equalTo("released", true)
            .or()
            .isNull("released")
            .endGroup()
            .beginGroup()
            .equalTo("owned", false)
            .or()
            .isNull("owned")
            .endGroup()
            .findAll()
            .toFlow()
            .map { it.count() }
    }

    override fun getOwnedEquipment(type: String): Flow<out List<Equipment>> {
        return realm.where(Equipment::class.java)
            .equalTo("type", type)
            .equalTo("owned", true)
            .findAll()
            .toFlow()
            .filter { it.isLoaded }
    }

    override fun getOwnedEquipment(): Flow<out List<Equipment>> {
        return realm.where(Equipment::class.java)
            .equalTo("owned", true)
            .findAll()
            .toFlow()
            .filter { it.isLoaded }
    }

    override fun getEquipmentType(type: String, set: String): Flow<out List<Equipment>> {
        return realm.where(Equipment::class.java)
            .equalTo("type", type)
            .equalTo("gearSet", set)
            .findAll()
            .toFlow()
            .filter { it.isLoaded }
    }

    override fun getOwnedItems(
        itemType: String,
        userID: String,
        includeZero: Boolean
    ): Flow<List<OwnedItem>> {
        return queryUser(userID).map {
            val items = when (itemType) {
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
    }

    override fun getItems(itemClass: Class<out Item>): Flow<List<Item>> {
        return realm.where(itemClass).findAll().toFlow()
            .filter { it.isLoaded }
    }

    override fun getItems(itemClass: Class<out Item>, keys: Array<String>): Flow<List<Item>> {
        return realm.where(itemClass).`in`("key", keys).findAll().toFlow()
            .filter { it.isLoaded }
    }

    override fun getOwnedItems(userID: String, includeZero: Boolean): Flow<Map<String, OwnedItem>> {
        return queryUser(userID)
            .filterNotNull()
            .map {
                val items = HashMap<String, OwnedItem>()
                it.items?.eggs?.forEach { items[it.key + "-" + it.itemType] = it }
                it.items?.food?.forEach { items[it.key + "-" + it.itemType] = it }
                it.items?.hatchingPotions?.forEach { items[it.key + "-" + it.itemType] = it }
                it.items?.quests?.forEach { items[it.key + "-" + it.itemType] = it }
                if (includeZero) {
                    items
                } else {
                    items.filter { it.value.numberOwned > 0 }
                }
            }
    }

    override fun getEquipment(key: String): Flow<Equipment> {
        return realm.where(Equipment::class.java)
            .equalTo("key", key)
            .findAll()
            .toFlow()
            .filter { realmObject -> realmObject.isLoaded && realmObject.isNotEmpty() }
            .map { it.first() }
            .filterNotNull()
    }

    override fun getMounts(): Flow<List<Mount>> {
        return realm.where(Mount::class.java)
            .sort("type", Sort.ASCENDING, "animal", Sort.ASCENDING)
            .findAll()
            .toFlow()
            .filter { it.isLoaded }
    }

    override fun getMounts(type: String?, group: String?, color: String?): Flow<List<Mount>> {
        var query = realm.where(Mount::class.java)
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
        return query.findAll()
            .toFlow()
            .filter { it.isLoaded }
    }

    override fun getOwnedMounts(userID: String): Flow<List<OwnedMount>> {
        return queryUser(userID)
            .map {
                it?.items?.mounts?.filter {
                    it.owned
                } ?: emptyList()
            }
    }

    override fun getPets(): Flow<List<Pet>> {
        return realm.where(Pet::class.java)
            .sort("type", Sort.ASCENDING, "animal", Sort.ASCENDING)
            .findAll()
            .toFlow()
            .filter { it.isLoaded }
    }

    override fun getPets(type: String?, group: String?, color: String?): Flow<List<Pet>> {
        var query = realm.where(Pet::class.java)
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
        return query.findAll()
            .toFlow()
            .filter { it.isLoaded }
    }

    override fun getOwnedPets(userID: String): Flow<List<OwnedPet>> {
        return realm.where(User::class.java)
            .equalTo("id", userID)
            .findAll()
            .toFlow()
            .filter { it.isLoaded && it.isValid && !it.isEmpty() }
            .map {
                it.first()?.items?.pets?.filter {
                    it.trained > 0
                } ?: emptyList()
            }
    }

    override fun updateOwnedEquipment(user: User) {
    }

    override suspend fun changeOwnedCount(
        type: String,
        key: String,
        userID: String,
        amountToAdd: Int
    ) {
        val item = getOwnedItem(userID, type, key, true).firstOrNull()
        if (item != null) {
            changeOwnedCount(item, amountToAdd)
        }
    }

    override fun changeOwnedCount(item: OwnedItem, amountToAdd: Int?) {
        val liveItem = getLiveObject(item) ?: return
        amountToAdd?.let { amount ->
            executeTransaction { liveItem.numberOwned = liveItem.numberOwned + amount }
        }
    }

    override fun getOwnedItem(
        userID: String,
        type: String,
        key: String,
        includeZero: Boolean
    ): Flow<OwnedItem> {
        return queryUser(userID)
            .filterNotNull()
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
            }
            .filter { it.isNotEmpty() }
            .map { it.first() }
    }

    override fun getItem(type: String, key: String): Flow<Item> {
        val itemClass: Class<out RealmObject> = when (type) {
            "eggs" -> Egg::class.java
            "hatchingPotions" -> HatchingPotion::class.java
            "food" -> Food::class.java
            "quests" -> QuestContent::class.java
            "special" -> SpecialItem::class.java
            else -> Egg::class.java
        }
        return realm.where(itemClass).equalTo("key", key)
            .findAll()
            .toFlow()
            .filter { realmObject -> realmObject.isLoaded && realmObject.isNotEmpty() }
            .map { it.firstOrNull() as? Item }
            .filterNotNull()
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

    override fun getInAppRewards(): Flow<List<ShopItem>> {
        return realm.where(ShopItem::class.java)
            .findAll()
            .toFlow()
            .filter { it.isLoaded }
    }

    override fun getInAppReward(key: String): Flow<ShopItem> {
        return realm.where(ShopItem::class.java)
            .equalTo("key", key)
            .findAll()
            .toFlow()
            .filter { it.isLoaded }
            .map { it.firstOrNull() }
            .filterNotNull()
    }

    override fun saveInAppRewards(onlineItems: List<ShopItem>) {
        val localItems = realm.where(ShopItem::class.java).findAll().createSnapshot()
        executeTransaction {
            for (localItem in localItems) {
                if (!onlineItems.contains(localItem)) {
                    localItem.deleteFromRealm()
                }
            }
            realm.insertOrUpdate(onlineItems)
        }
    }

    override fun hatchPet(eggKey: String, potionKey: String, userID: String) {
        val newPet = OwnedPet()
        newPet.key = "$eggKey-$potionKey"
        newPet.trained = 5
        val user = realm.where(User::class.java).equalTo("id", userID).findFirst() ?: return
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
        if (isClosed) return null
        if (!obj.isManaged) return obj
        return realm.where(OwnedItem::class.java).equalTo("key", obj.key)
            .equalTo("itemType", obj.itemType).findFirst()
    }

    override fun save(items: Items, userID: String) {
        val user = realm.where(User::class.java).equalTo("id", userID).findFirst() ?: return
        items.setItemTypes()
        executeTransaction {
            user.items = items
        }
    }

    override fun unhatchPet(eggKey: String, potionKey: String, userID: String) {
        val pet = realm.where(OwnedPet::class.java).equalTo("key", "$eggKey-$potionKey").findFirst()
        val user = realm.where(User::class.java).equalTo("id", userID).findFirst() ?: return
        val egg = user.items?.eggs?.firstOrNull { it.key == eggKey } ?: return
        val hatchingPotion =
            user.items?.hatchingPotions?.firstOrNull { it.key == potionKey } ?: return
        executeTransaction {
            egg.numberOwned += 1
            hatchingPotion.numberOwned += 1
            user.items?.pets?.remove(pet)
        }
    }

    override fun feedPet(foodKey: String, petKey: String, feedValue: Int, userID: String) {
        val user = realm.where(User::class.java).equalTo("id", userID).findFirst() ?: return
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

    override fun getLatestMysteryItem(): Flow<Equipment> {
        return realm.where(Equipment::class.java)
            .contains("key", "mystery_2")
            .sort("mystery", Sort.DESCENDING)
            .findAll()
            .toFlow()
            .filter { it.isLoaded && it.size > 0 }
            .map {
                val format = SimpleDateFormat("yyyyMM", Locale.US)
                it.first {
                    it.key?.contains(format.format(Date())) == true
                }
            }
    }

    override fun soldItem(userID: String, updatedUser: User): User {
        val user = realm.where(User::class.java)
            .equalTo("id", userID)
            .findFirst() ?: return updatedUser
        executeTransaction {
            val items = updatedUser.items
            if (items != null) {
                user.items = items
            }
            val stats = updatedUser.stats
            if (stats != null) {
                user.stats = stats
            }
        }
        return user
    }

    override fun getAvailableLimitedItems(): Flow<List<Item>> {
        return realm.where(Egg::class.java)
            .lessThan("event.start", Date())
            .greaterThan("event.end", Date())
            .findAll().toFlow()
            .map {
                val items = mutableListOf<Item>()
                items.addAll(it)
                items
            }
            .combine(
                realm.where(Food::class.java)
                    .lessThan("event.start", Date())
                    .greaterThan("event.end", Date())
                    .findAll().toFlow()
            ) { items, food ->
                items.addAll(food)
                items
            }
            .combine(
                realm.where(HatchingPotion::class.java)
                    .lessThan("event.start", Date())
                    .greaterThan("event.end", Date())
                    .findAll().toFlow()
            ) { items, food ->
                items.addAll(food)
                items
            }
            .combine(
                realm.where(QuestContent::class.java)
                    .lessThan("event.start", Date())
                    .greaterThan("event.end", Date())
                    .findAll().toFlow()
            ) { items, food ->
                items.addAll(food)
                items
            }
    }
}
