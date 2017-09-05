package com.habitrpg.android.habitica.data.local;

import com.habitrpg.android.habitica.models.inventory.Equipment;
import com.habitrpg.android.habitica.models.inventory.Item;
import com.habitrpg.android.habitica.models.inventory.Mount;
import com.habitrpg.android.habitica.models.inventory.Pet;
import com.habitrpg.android.habitica.models.inventory.QuestContent;
import com.habitrpg.android.habitica.models.shops.ShopItem;
import com.habitrpg.android.habitica.models.user.User;

import java.util.List;

import io.realm.RealmResults;
import rx.Observable;

public interface InventoryLocalRepository extends ContentLocalRepository {
    Observable<QuestContent> getQuestContent(String key);

    Observable<RealmResults<Equipment>> getEquipment(List<String> searchedKeys);

    long getArmoireRemainingCount();

    Observable<RealmResults<Equipment>> getOwnedEquipment(String type);
    Observable<RealmResults<Equipment>> getOwnedEquipment();

    Observable<? extends RealmResults<? extends Item>> getOwnedItems(String itemType, User user);

    Observable<Equipment> getEquipment(String key);

    Observable<RealmResults<Mount>> getMounts();
    Observable<RealmResults<Mount>> getMounts(String type, String group);

    Observable<RealmResults<Mount>> getOwnedMounts();
    Observable<RealmResults<Mount>> getOwnedMounts(String animalType, String animalGroup);

    Observable<RealmResults<Pet>> getPets();
    Observable<RealmResults<Pet>> getPets(String type, String group);

    Observable<RealmResults<Pet>> getOwnedPets();
    Observable<RealmResults<Pet>> getOwnedPets(String type, String group);

    void updateOwnedEquipment(User user);

    void changeOwnedCount(String type, String key, int amountToAdd);
    void changeOwnedCount(Item item, Integer amountToAdd);

    Observable<Item> getItem(String type, String key);

    void decrementMysteryItemCount(User user);

    Observable<RealmResults<ShopItem>> getInAppRewards();
    void saveInAppRewards(List<ShopItem> onlineItems);
}
