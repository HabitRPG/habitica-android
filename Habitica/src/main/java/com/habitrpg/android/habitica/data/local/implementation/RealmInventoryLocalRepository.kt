package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.InventoryLocalRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.*
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.user.*
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Flowable
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.Sort
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class RealmInventoryLocalRepository(realm: Realm) : RealmContentLocalRepository(realm), InventoryLocalRepository {
    override fun getQuestContent(keys: List<String>): Flowable<out List<QuestContent>> {
        return RxJavaBridge.toV3Flowable(realm.where(QuestContent::class.java)
                .`in`("key", keys.toTypedArray())
                .findAll()
                .asFlowable()
                .filter { it.isLoaded })
    }

    override fun getQuestContent(key: String): Flowable<QuestContent> {
        return RxJavaBridge.toV3Flowable(realm.where(QuestContent::class.java).equalTo("key", key)
                .findAll()
                .asFlowable()
                .filter { content -> content.isLoaded && content.isValid && !content.isEmpty() }
                .map { content -> content.first() })
    }

    override fun getEquipment(searchedKeys: List<String>): Flowable<out List<Equipment>> {
        return RxJavaBridge.toV3Flowable(realm.where(Equipment::class.java)
                .`in`("key", searchedKeys.toTypedArray())
                .findAll()
                .asFlowable()
                .filter { it.isLoaded })
    }

    override fun getArmoireRemainingCount(): Long {
        return realm.where(Equipment::class.java)
                .equalTo("klass", "armoire")
                .beginGroup()
                .equalTo("owned", false)
                .or()
                .isNull("owned")
                .endGroup()
                .count()
    }

    override fun getOwnedEquipment(type: String): Flowable<out List<Equipment>> {
        return RxJavaBridge.toV3Flowable(realm.where(Equipment::class.java)
                .equalTo("type", type)
                .equalTo("owned", true)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded })
    }

    override fun getOwnedEquipment(): Flowable<out List<Equipment>> {
        return RxJavaBridge.toV3Flowable(realm.where(Equipment::class.java)
                .equalTo("owned", true)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded })
    }

    override fun getEquipmentType(type: String, set: String): Flowable<out List<Equipment>> {
        return RxJavaBridge.toV3Flowable(realm.where(Equipment::class.java)
                .equalTo("type", type)
                .equalTo("gearSet", set)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded })
    }

    override fun getOwnedItems(itemType: String, userID: String, includeZero: Boolean): Flowable<out List<OwnedItem>> {
        return queryUser(userID).map {
            val items = when (itemType) {
                "eggs" -> it.items?.eggs
                "hatchingPotions" -> it.items?.hatchingPotions
                "food" -> it.items?.food
                "quests" -> it.items?.quests
                else -> emptyList()
            } ?: emptyList()
            if (includeZero) {
                items
            } else {
                items.filter { it.numberOwned > 0 }
            }
        }
    }

    override fun getItems(itemClass: Class<out Item>, keys: Array<String>): Flowable<out List<Item>> {
        return RxJavaBridge.toV3Flowable(realm.where(itemClass).`in`("key", keys).findAll().asFlowable()
                .filter { it.isLoaded })
    }

    override fun getItems(itemClass: Class<out Item>): Flowable<out List<Item>> {
        return RxJavaBridge.toV3Flowable(realm.where(itemClass).findAll().asFlowable()
                .filter { it.isLoaded })
    }

    override fun getOwnedItems(userID: String, includeZero: Boolean): Flowable<Map<String, OwnedItem>> {
        return queryUser(userID).map {
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

    override fun getEquipment(key: String): Flowable<Equipment> {
        return RxJavaBridge.toV3Flowable(realm.where(Equipment::class.java)
                .equalTo("key", key)
                .findFirstAsync()
                .asFlowable<RealmObject>()
                .filter { realmObject -> realmObject.isLoaded }
                .cast(Equipment::class.java))
    }

    override fun getMounts(): Flowable<out List<Mount>> {
        return RxJavaBridge.toV3Flowable(realm.where(Mount::class.java)
                .sort("type", Sort.ASCENDING, "animal", Sort.ASCENDING)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded })
    }

    override fun getMounts(type: String?, group: String?, color: String?): Flowable<out List<Mount>> {
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
        return RxJavaBridge.toV3Flowable(query.findAll()
                .asFlowable()
                .filter { it.isLoaded })
    }

    override fun getOwnedMounts(userID: String): Flowable<out List<OwnedMount>> {
        return queryUser(userID)
            .map { it.items?.mounts?.filter {
                it.owned == true
            } ?: emptyList() }
    }


    override fun getPets(): Flowable<out List<Pet>> {
        return RxJavaBridge.toV3Flowable(realm.where(Pet::class.java)
                .sort("type", Sort.ASCENDING, "animal", Sort.ASCENDING)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded })
    }

    override fun getPets(type: String?, group: String?, color: String?): Flowable<out List<Pet>> {
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
        return RxJavaBridge.toV3Flowable(query.findAll()
                .asFlowable()
                .filter { it.isLoaded })
    }

    override fun getOwnedPets(userID: String): Flowable<out List<OwnedPet>> {
        return RxJavaBridge.toV3Flowable(realm.where(User::class.java)
                .equalTo("id", userID)
                .findAll()
                .asFlowable())
            .filter { it.isLoaded && it.isValid && !it.isEmpty() }
            .map { it.first()?.items?.pets?.filter {
                it.trained > 0
            } ?: emptyList() }
    }

    override fun updateOwnedEquipment(user: User) {

    }

    override fun changeOwnedCount(type: String, key: String, userID: String, amountToAdd: Int) {
        getOwnedItem(userID, type, key, true).firstElement().subscribe({ changeOwnedCount(it, amountToAdd)}, RxErrorHandler.handleEmptyError())
    }

    override fun changeOwnedCount(item: OwnedItem, amountToAdd: Int?) {
        val liveItem = getLiveObject(item) ?: return
        amountToAdd?.let { amount ->
            executeTransaction { liveItem.numberOwned = liveItem.numberOwned + amount }
        }
    }

    override fun getOwnedItem(userID: String, type: String, key: String, includeZero: Boolean): Flowable<OwnedItem> {
        return queryUser(userID).map {
            var items = (when (type) {
                "eggs" -> it.items?.eggs
                "hatchingPotions" -> it.items?.hatchingPotions
                "food" -> it.items?.food
                "quests" -> it.items?.quests
                else -> emptyList()
            } ?: emptyList())
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

    override fun getItem(type: String, key: String): Flowable<Item> {
        val itemClass: Class<out RealmObject> = when (type) {
            "eggs" -> Egg::class.java
            "hatchingPotions" -> HatchingPotion::class.java
            "food" -> Food::class.java
            "quests" -> QuestContent::class.java
            "special" -> SpecialItem::class.java
            else -> Egg::class.java
        }
        return RxJavaBridge.toV3Flowable(realm.where(itemClass).equalTo("key", key).findFirstAsync().asFlowable<RealmObject>()
                .filter { realmObject -> realmObject.isLoaded }
                .cast(Item::class.java))
    }

    override fun decrementMysteryItemCount(user: User?) {
        if (user == null) {
            return
        }
        val item = realm.where(OwnedItem::class.java).equalTo("combinedKey", "${user.id}specialinventory_present").findFirst()
        val liveUser = getLiveObject(user)
        executeTransaction {
            if (item != null && item.isValid) {
                item.numberOwned = item.numberOwned - 1
            }
            if (liveUser?.isValid == true) {
                liveUser.purchased?.plan?.mysteryItemCount = (user.purchased?.plan?.mysteryItemCount ?: 0) - 1
            }
        }
    }

    override fun getInAppRewards(): Flowable<out List<ShopItem>> {
        return RxJavaBridge.toV3Flowable(realm.where(ShopItem::class.java)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded })
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
        val hatchingPotion = user.items?.hatchingPotions?.firstOrNull { it.key == potionKey } ?: return
        executeTransaction {
            egg.numberOwned -= 1
            hatchingPotion.numberOwned -= 1
            user.items?.pets?.add(newPet)
        }
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
        val hatchingPotion = user.items?.hatchingPotions?.firstOrNull { it.key == potionKey } ?: return
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

    override fun getLatestMysteryItem(): Flowable<Equipment> {
        return RxJavaBridge.toV3Flowable(realm.where(Equipment::class.java)
                .contains("key", "mystery_2")
                .sort("mystery", Sort.DESCENDING)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded && it.size > 0}
                .map {
                    val format = SimpleDateFormat("yyyyMM", Locale.US)
                    it.first {
                        it.key?.contains(format.format(Date())) == true
                    }
                })
    }

    override fun soldItem(userID: String, updatedUser: User): User {
        val user = realm.where(User::class.java)
                .equalTo("id", userID)
                .findFirst() ?: return updatedUser
        executeTransaction {
            val items = updatedUser.items
            if (items != null) {
                val newItems = realm.copyToRealmOrUpdate(items)
                user.items = newItems
            }
            val stats = updatedUser.stats
            if (stats != null) {
                val newStats = realm.copyToRealmOrUpdate(stats)
                user.stats = newStats
            }
        }
        return user
    }

    override fun getAvailableLimitedItems(): Flowable<List<Item>> {
        return Flowable.combineLatest(
                realm.where(Egg::class.java)
                        .lessThan("event.start", Date())
                        .greaterThan("event.end", Date())
                        .findAll().asFlowable(),
                realm.where(Food::class.java)
                        .lessThan("event.start", Date())
                        .greaterThan("event.end", Date())
                        .findAll().asFlowable(),
                realm.where(HatchingPotion::class.java)
                        .lessThan("event.start", Date())
                        .greaterThan("event.end", Date())
                        .findAll().asFlowable(),
                realm.where(QuestContent::class.java)
                        .lessThan("event.start", Date())
                        .greaterThan("event.end", Date())
                        .findAll().asFlowable(),
                { eggs, food, potions, quests ->
                    val items = mutableListOf<Item>()
                    items.addAll(eggs)
                    items.addAll(food)
                    items.addAll(potions)
                    items.addAll(quests)
                    items
                }
        )
    }
}
