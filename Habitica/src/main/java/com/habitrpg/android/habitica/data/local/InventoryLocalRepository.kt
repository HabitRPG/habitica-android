package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.inventory.*
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.OwnedMount
import com.habitrpg.android.habitica.models.user.OwnedPet
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.Flowable
import io.realm.RealmResults

interface InventoryLocalRepository : ContentLocalRepository {

    fun getArmoireRemainingCount(): Long
    fun getOwnedEquipment(): Flowable<RealmResults<Equipment>>

    fun getMounts(): Flowable<RealmResults<Mount>>

    fun getOwnedMounts(userID: String): Flowable<RealmResults<OwnedMount>>

    fun getPets(): Flowable<RealmResults<Pet>>

    fun getOwnedPets(userID: String): Flowable<RealmResults<OwnedPet>>

    fun getInAppRewards(): Flowable<RealmResults<ShopItem>>
    fun getQuestContent(key: String): Flowable<QuestContent>
    fun getQuestContent(keys: List<String>): Flowable<RealmResults<QuestContent>>

    fun getEquipment(searchedKeys: List<String>): Flowable<RealmResults<Equipment>>

    fun getOwnedEquipment(type: String): Flowable<RealmResults<Equipment>>

    fun getItems(itemClass: Class<out Item>, keys: Array<String>, user: User?): Flowable<out RealmResults<out Item>>
    fun getOwnedItems(itemType: String, userID: String): Flowable<RealmResults<OwnedItem>>
    fun getOwnedItems(userID: String): Flowable<Map<String, OwnedItem>>

    fun getEquipment(key: String): Flowable<Equipment>
    fun getMounts(type: String, group: String, color: String?): Flowable<RealmResults<Mount>>
    fun getPets(type: String, group: String, color: String?): Flowable<RealmResults<Pet>>

    fun updateOwnedEquipment(user: User)

    fun changeOwnedCount(type: String, key: String, userID: String, amountToAdd: Int)
    fun changeOwnedCount(item: OwnedItem, amountToAdd: Int?)

    fun getItem(type: String, key: String): Flowable<Item>
    fun getOwnedItem(userID: String, type: String, key: String): Flowable<OwnedItem>

    fun decrementMysteryItemCount(user: User?)
    fun saveInAppRewards(onlineItems: List<ShopItem>)

    fun changePetFeedStatus(key: String?, userID: String, feedStatus: Int)
    fun hatchPet(eggKey: String, potionKey: String, userID: String)
    fun unhatchPet(eggKey: String, potionKey: String, userID: String)
    fun feedPet(foodKey: String, petKey: String, feedValue: Int, userID: String)
    fun getLatestMysteryItem(): Flowable<Equipment>
}
