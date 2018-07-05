package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.inventory.*
import com.habitrpg.android.habitica.models.shops.ShopItem
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

    fun getOwnedItems(itemClass: Class<out Item>, user: User?): Flowable<out RealmResults<out Item>>
    fun getOwnedItems(user: User): Flowable<out Map<String, Item>>

    fun getEquipment(key: String): Flowable<Equipment>
    fun getMounts(type: String, group: String): Flowable<RealmResults<Mount>>
    fun getOwnedMounts(animalType: String, animalGroup: String): Flowable<RealmResults<Mount>>
    fun getPets(type: String, group: String): Flowable<RealmResults<Pet>>
    fun getOwnedPets(type: String, group: String): Flowable<RealmResults<Pet>>

    fun updateOwnedEquipment(user: User)

    fun changeOwnedCount(type: String, key: String, amountToAdd: Int)
    fun changeOwnedCount(item: Item, amountToAdd: Int?)

    fun getItem(type: String, key: String): Flowable<Item>

    fun decrementMysteryItemCount(user: User)
    fun saveInAppRewards(onlineItems: List<ShopItem>)

    fun changePetFeedStatus(key: String?, feedStatus: Int)
}
