package com.habitrpg.android.habitica.data.local.implementation

import android.content.Context
import com.habitrpg.android.habitica.data.local.InventoryLocalRepository
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.*
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.Flowable
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function4
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.Sort


class RealmInventoryLocalRepository(realm: Realm, private val context: Context) : RealmContentLocalRepository(realm), InventoryLocalRepository {

    override fun getQuestContent(key: String): Flowable<QuestContent> {
        return realm.where(QuestContent::class.java).equalTo("key", key).findFirstAsync()
                .asFlowable<RealmObject>()
                .filter { realmObject -> realmObject.isLoaded }
                .cast(QuestContent::class.java)
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

    override fun getOwnedItems(itemClass: Class<out Item>, user: User?): Flowable<out RealmResults<out Item>> {
        var query = realm.where(itemClass)
        if (SpecialItem::class.java.isAssignableFrom(itemClass)) {
            if (user?.purchased?.plan != null) {
                val mysticItem: SpecialItem = if (query.count() == 0L) {
                    SpecialItem.makeMysteryItem(context)
                } else {
                    getUnmanagedCopy((query.findFirst() as SpecialItem?)!!)
                }
                mysticItem.owned = user.purchased?.plan?.mysteryItemCount
                this.save(mysticItem)
            }
        } else {
            query = query.greaterThan("owned", 0)
        }
        return query.findAllAsync().asFlowable()
                .filter { it.isLoaded }
    }

    override fun getOwnedItems(user: User): Flowable<out Map<String, Item>> {
        return Flowable.combineLatest(
                getOwnedItems(Egg::class.java, user),
                getOwnedItems(HatchingPotion::class.java, user),
                getOwnedItems(Food::class.java, user),
                getOwnedItems(QuestContent::class.java, user),
                Function4 { eggs, hatchingPotions, food, quests ->
                    val items = HashMap<String, Item>()
                    for (item in eggs) {
                        items[item.key + "-" + item.type] = item
                    }
                    for (item in hatchingPotions) {
                        items[item.key + "-" + item.type] = item
                    }
                    for (item in food) {
                        items[item.key + "-" + item.type] = item
                    }
                    for (item in quests) {
                        items[item.key + "-" + item.type] = item
                    }
                    items
                }
        )
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
                .sort("animalGroup", Sort.ASCENDING, "animal", Sort.ASCENDING)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun getMounts(type: String, group: String): Flowable<RealmResults<Mount>> {
        return realm.where(Mount::class.java)
                .sort("color", Sort.ASCENDING)
                .equalTo("animalGroup", group)
                .equalTo("animal", type)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun getOwnedMounts(): Flowable<RealmResults<Mount>> {
        return realm.where(Mount::class.java)
                .equalTo("owned", true)
                .sort("animalGroup", Sort.ASCENDING, "animal", Sort.ASCENDING)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun getOwnedMounts(animalType: String, animalGroup: String): Flowable<RealmResults<Mount>> {
        val thisAnimalGroup = animalGroup.replace("pets", "mounts").replace("Pets", "Mounts")
        return realm.where(Mount::class.java)
                .equalTo("animalGroup", thisAnimalGroup)
                .equalTo("animal", animalType)
                .equalTo("owned", true)
                .sort("animal")
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun getPets(): Flowable<RealmResults<Pet>> {
        return realm.where(Pet::class.java)
                .sort("animalGroup", Sort.ASCENDING, "animal", Sort.ASCENDING)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun getPets(type: String, group: String): Flowable<RealmResults<Pet>> {
        return realm.where(Pet::class.java)
                .sort("color", Sort.ASCENDING)
                .equalTo("animalGroup", group)
                .equalTo("animal", type)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun getOwnedPets(): Flowable<RealmResults<Pet>> {
        return realm.where(Pet::class.java)
                .greaterThan("trained", 0)
                .sort("animalGroup", Sort.ASCENDING, "animal", Sort.ASCENDING)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun getOwnedPets(type: String, group: String): Flowable<RealmResults<Pet>> {
        return realm.where(Pet::class.java)
                .equalTo("animalGroup", group)
                .equalTo("animal", type)
                .greaterThan("trained", 0)
                .sort("animal")
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun updateOwnedEquipment(user: User) {

    }

    override fun changeOwnedCount(type: String, key: String, amountToAdd: Int) {
        getItem(type, key).firstElement().subscribe( Consumer { changeOwnedCount(it, amountToAdd)}, RxErrorHandler.handleEmptyError())
    }

    override fun changeOwnedCount(item: Item, amountToAdd: Int?) {
        amountToAdd.notNull { amount ->
            realm.executeTransaction { item.owned = item.owned + amount }
        }
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
        val item = realm.where(SpecialItem::class.java).equalTo("isMysteryItem", true).findFirst()
        realm.executeTransaction {
            if (item != null && item.isValid) {
                item.owned = item.owned - 1
            }
            if (user.isValid) {
                user.purchased?.plan?.mysteryItemCount = (user.purchased?.plan?.mysteryItemCount ?: 0) - 1
            }
        }
    }

    override fun getInAppRewards(): Flowable<RealmResults<ShopItem>> {
        return realm.where(ShopItem::class.java)
                .findAllAsync()
                .asFlowable()
                .filter { it.isLoaded }
    }

    override fun saveInAppRewards(onlineItems: List<ShopItem>) {
        val localItems = realm.where(ShopItem::class.java).findAll().createSnapshot()
        realm.executeTransaction {
            for (localItem in localItems) {
                if (!onlineItems.contains(localItem)) {
                    localItem.deleteFromRealm()
                }
            }
            realm.insertOrUpdate(onlineItems)
        }
    }

    override fun changePetFeedStatus(key: String?, feedStatus: Int) {
        val pet = realm.where(Pet::class.java).equalTo("key", key).findFirst()
        if (pet != null) {
            executeTransaction { pet.trained = feedStatus }
        }
    }
}
