package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.inventory.*
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.Flowable
import io.realm.RealmResults

interface InventoryLocalRepository : ContentLocalRepository {

    fun getArmoireRemainingCount(): Long
    fun getOwnedEquipment(): Flowable<RealmResults<Equipment>>

    fun getMounts(): Flowable<RealmResults<Mount>>

    fun getOwnedMounts(): Flowable<RealmResults<Mount>>

    fun getPets(): Flowable<RealmResults<Pet>>

    fun getOwnedPets(): Flowable<RealmResults<Pet>>

    fun getInAppRewards(): Flowable<RealmResults<ShopItem>>
    fun getQuestContent(key: String): Flowable<QuestContent>

    fun getEquipment(searchedKeys: List<String>): Flowable<RealmResults<Equipment>>

    fun getOwnedEquipment(type: String): Flowable<RealmResults<Equipment>>

    fun getItems(itemClass: Class<out Item>, keys: Array<String>, user: User?): Flowable<out RealmResults<out Item>>
    fun getOwnedItems(itemType: String, userID: String): Flowable<RealmResults<OwnedItem>>
    fun getOwnedItems(user: User): Flowable<Map<String, OwnedItem>>

    fun getEquipment(key: String): Flowable<Equipment>
    fun getMounts(type: String, group: String): Flowable<RealmResults<Mount>>
    fun getOwnedMounts(animalType: String, animalGroup: String): Flowable<RealmResults<Mount>>
    fun getPets(type: String, group: String): Flowable<RealmResults<Pet>>
    fun getOwnedPets(type: String, group: String): Flowable<RealmResults<Pet>>

    fun updateOwnedEquipment(user: User)

    fun changeOwnedCount(type: String, key: String, userID: String, amountToAdd: Int)
    fun changeOwnedCount(item: OwnedItem, amountToAdd: Int?)

    fun getItem(type: String, key: String): Flowable<Item>

    fun decrementMysteryItemCount(user: User?)
    fun saveInAppRewards(onlineItems: List<ShopItem>)

    fun changePetFeedStatus(key: String?, feedStatus: Int)
}
