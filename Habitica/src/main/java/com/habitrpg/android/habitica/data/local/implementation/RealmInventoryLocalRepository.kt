package com.habitrpg.android.habitica.data.local.implementation

import android.content.Context
import com.habitrpg.android.habitica.data.local.InventoryLocalRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.*
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.OwnedMount
import com.habitrpg.android.habitica.models.user.OwnedPet
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.Flowable
import io.reactivex.functions.Consumer
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.Sort


class RealmInventoryLocalRepository(realm: Realm, private val context: Context) : RealmContentLocalRepository(realm), InventoryLocalRepository {
    override fun getQuestContent(keys: List<String>): Flowable<RealmResults<QuestContent>> {
        return realm.where(QuestContent::class.java)
                .`in`("key", keys.toTypedArray())
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun getQuestContent(key: String): Flowable<QuestContent> {
        return realm.where(QuestContent::class.java).equalTo("key", key)
                .findAll()
                .asFlowable()
                .filter { content -> content.isLoaded && content.isValid && !content.isEmpty() }
                .map { content -> content.first() }
    }

    override fun getEquipment(searchedKeys: List<String>): Flowable<RealmResults<Equipment>> {
        return realm.where(Equipment::class.java)
                .`in`("key", searchedKeys.toTypedArray())
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
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

    override fun getOwnedEquipment(type: String): Flowable<RealmResults<Equipment>> {
        return realm.where(Equipment::class.java)
                .equalTo("type", type)
                .equalTo("owned", true)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun getOwnedEquipment(): Flowable<RealmResults<Equipment>> {
        return realm.where(Equipment::class.java)
                .equalTo("owned", true)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun getOwnedItems(itemType: String, userID: String): Flowable<RealmResults<OwnedItem>> {
        return realm.where(OwnedItem::class.java)
                .greaterThan("numberOwned", 0)
                .equalTo("itemType", itemType)
                .equalTo("userID", userID)
                .sort("key")
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun getItems(itemClass: Class<out Item>, keys: Array<String>, user: User?): Flowable<out RealmResults<out Item>> {
        return realm.where(itemClass).`in`("key", keys).findAll().asFlowable()
                .filter { it.isLoaded }
    }

    override fun getOwnedItems(userID: String): Flowable<Map<String, OwnedItem>> {
        return realm.where(OwnedItem::class.java)
                .greaterThan("numberOwned", 0)
                .equalTo("userID", userID)
                .findAll()
                .asFlowable()
                .map {
                    val items = HashMap<String, OwnedItem>()
                    for (item in it) {
                        items[item.key + "-" + item.itemType] = item
                    }
                    items
                }
    }

    override fun getEquipment(key: String): Flowable<Equipment> {
        return realm.where(Equipment::class.java)
                .sort("text")
                .equalTo("key", key)
                .findFirstAsync()
                .asFlowable<RealmObject>()
                .filter { realmObject -> realmObject.isLoaded }
                .cast(Equipment::class.java)
    }

    override fun getMounts(): Flowable<RealmResults<Mount>> {
        return realm.where(Mount::class.java)
                .sort("type", Sort.ASCENDING, "animal", Sort.ASCENDING)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun getMounts(type: String, group: String, color: String?): Flowable<RealmResults<Mount>> {
        var query = realm.where(Mount::class.java)
                .sort("color", Sort.ASCENDING)
                .equalTo("type", group)
                .equalTo("animal", type)
        if (color != null) {
            query = query.equalTo("color", color)
        }
        return query.findAll()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun getOwnedMounts(userID: String): Flowable<RealmResults<OwnedMount>> {
        return realm.where(OwnedMount::class.java)
                .equalTo("owned", true)
                .equalTo("userID", userID)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun getPets(): Flowable<RealmResults<Pet>> {
        return realm.where(Pet::class.java)
                .sort("type", Sort.ASCENDING, "animal", Sort.ASCENDING)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun getPets(type: String, group: String, color: String?): Flowable<RealmResults<Pet>> {
        var query = realm.where(Pet::class.java)
                .sort("color", Sort.ASCENDING)
                .equalTo("type", group)
                .equalTo("animal", type)
        if (color != null) {
            query = query.equalTo("color", color)
        }
        return query.findAll()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun getOwnedPets(userID: String): Flowable<RealmResults<OwnedPet>> {
        return realm.where(OwnedPet::class.java)
                .greaterThan("trained", 0)
                .equalTo("userID", userID)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun updateOwnedEquipment(user: User) {

    }

    override fun changeOwnedCount(type: String, key: String, userID: String, amountToAdd: Int) {
        getOwnedItem(userID, type, key).firstElement().subscribe( Consumer { changeOwnedCount(it, amountToAdd)}, RxErrorHandler.handleEmptyError())
    }

    override fun changeOwnedCount(item: OwnedItem, amountToAdd: Int?) {
        amountToAdd?.let { amount ->
            executeTransaction { item.numberOwned = item.numberOwned + amount }
        }
    }

    override fun getOwnedItem(userID: String, type: String, key: String): Flowable<OwnedItem> {
        return realm.where(OwnedItem::class.java)
                .equalTo("itemType", type)
                .equalTo("key", key)
                .equalTo("userID", userID)
                .greaterThan("numberOwned", 0)
                .findFirstAsync()
                .asFlowable<OwnedItem>()
                .filter { realmObject -> realmObject.isLoaded }
    }

    override fun getItem(type: String, key: String): Flowable<Item> {
        val itemClass: Class<out RealmObject> = when (type) {
            "eggs" -> Egg::class.java
            "hatchingPotions" -> HatchingPotion::class.java
            "food" -> Food::class.java
            "quests" -> QuestContent::class.java
            else -> Egg::class.java
        }
        return realm.where(itemClass).equalTo("key", key).findFirstAsync().asFlowable<RealmObject>()
                .filter { realmObject -> realmObject.isLoaded }
                .cast(Item::class.java)
    }

    override fun decrementMysteryItemCount(user: User?) {
        if (user == null) {
            return
        }
        val item = realm.where(OwnedItem::class.java).equalTo("combinedKey", "${user.id}specialinventory_present").findFirst()
        executeTransaction {
            if (item != null && item.isValid) {
                item.numberOwned = item.numberOwned - 1
            }
            if (user.isValid) {
                user.purchased?.plan?.mysteryItemCount = (user.purchased?.plan?.mysteryItemCount ?: 0) - 1
            }
        }
    }

    override fun getInAppRewards(): Flowable<RealmResults<ShopItem>> {
        return realm.where(ShopItem::class.java)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
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
        newPet.userID = userID
        newPet.trained = 5
        val egg = realm.where(OwnedItem::class.java)
                .equalTo("itemType", "eggs")
                .equalTo("key", eggKey)
                .equalTo("userID", userID)
                .greaterThan("numberOwned", 0)
                .findFirst() ?: return
        val hatchingPotion = realm.where(OwnedItem::class.java)
                .equalTo("itemType", "hatchingPotions")
                .equalTo("key", potionKey)
                .equalTo("userID", userID)
                .greaterThan("numberOwned", 0)
                .findFirst() ?: return
        executeTransaction {
            egg.numberOwned -= 1
            hatchingPotion.numberOwned -= 1
            it.insertOrUpdate(newPet)
        }
    }

    override fun unhatchPet(eggKey: String, potionKey: String, userID: String) {
        val pet = realm.where(OwnedPet::class.java).equalTo("key", "$eggKey-$potionKey").findFirst()
        val egg = realm.where(OwnedItem::class.java)
                .equalTo("itemType", "eggs")
                .equalTo("key", eggKey)
                .equalTo("userID", userID)
                .findFirst() ?: return
        val hatchingPotion = realm.where(OwnedItem::class.java)
                .equalTo("itemType", "hatchingPotions")
                .equalTo("key", potionKey)
                .equalTo("userID", userID)
                .findFirst() ?: return
        executeTransaction {
            egg.numberOwned += 1
            hatchingPotion.numberOwned += 1
            pet?.deleteFromRealm()
        }
    }

    override fun feedPet(foodKey: String, petKey: String, feedValue: Int, userID: String) {
        val pet = realm.where(OwnedPet::class.java).equalTo("key", petKey).findFirst() ?: return
        val food = realm.where(OwnedItem::class.java).equalTo("key", foodKey).equalTo("itemType", "food").findFirst() ?: return
        executeTransaction {
            pet.trained = feedValue
            food.numberOwned -= 1

            if (feedValue < 0) {
                val mount = OwnedMount()
                mount.key = petKey
                mount.userID = userID
                mount.owned = true
                it.insertOrUpdate(mount)
            }
        }
    }

    override fun changePetFeedStatus(key: String?, userID: String, feedStatus: Int) {
        val newPet = OwnedPet()
        newPet.key = key
        newPet.userID = userID
        newPet.trained = feedStatus
        executeTransaction {
            it.insertOrUpdate(newPet)
        }
    }

    override fun getLatestMysteryItem(): Flowable<Equipment> {
        return realm.where(Equipment::class.java)
                .beginsWith("key", "armor_mystery_2")
                .sort("key", Sort.DESCENDING)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded && it.size > 0}
                .map { it.first() }
    }
}
