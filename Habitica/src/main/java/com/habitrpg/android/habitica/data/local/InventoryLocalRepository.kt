package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.inventory.Item
import com.habitrpg.android.habitica.models.inventory.Mount
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.user.User

import io.realm.RealmResults
import rx.Observable

interface InventoryLocalRepository : ContentLocalRepository {

    fun getArmoireRemainingCount(): Long
    fun getOwnedEquipment(): Observable<RealmResults<Equipment>>

    fun getMounts(): Observable<RealmResults<Mount>>

    fun getOwnedMounts(): Observable<RealmResults<Mount>>

    fun getPets(): Observable<RealmResults<Pet>>

    fun getOwnedPets(): Observable<RealmResults<Pet>>

    fun getInAppRewards(): Observable<RealmResults<ShopItem>>
    fun getQuestContent(key: String): Observable<QuestContent>

    fun getEquipment(searchedKeys: List<String>): Observable<RealmResults<Equipment>>

    fun getOwnedEquipment(type: String): Observable<RealmResults<Equipment>>

    fun getOwnedItems(itemClass: Class<out Item>, user: User): Observable<out RealmResults<out Item>>
    fun getOwnedItems(user: User): Observable<out Map<String, Item>>

    fun getEquipment(key: String): Observable<Equipment>
    fun getMounts(type: String, group: String): Observable<RealmResults<Mount>>
    fun getOwnedMounts(animalType: String, animalGroup: String): Observable<RealmResults<Mount>>
    fun getPets(type: String, group: String): Observable<RealmResults<Pet>>
    fun getOwnedPets(type: String, group: String): Observable<RealmResults<Pet>>

    fun updateOwnedEquipment(user: User)

    fun changeOwnedCount(type: String, key: String, amountToAdd: Int)
    fun changeOwnedCount(item: Item, amountToAdd: Int?)

    fun getItem(type: String, key: String): Observable<Item>

    fun decrementMysteryItemCount(user: User)
    fun saveInAppRewards(onlineItems: List<ShopItem>)

    fun changePetFeedStatus(key: String?, feedStatus: Int)
}
