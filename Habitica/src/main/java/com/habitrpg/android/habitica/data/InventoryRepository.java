package com.habitrpg.android.habitica.data;

import android.support.annotation.Nullable;

import com.habitrpg.android.habitica.models.inventory.Egg;
import com.habitrpg.android.habitica.models.inventory.Food;
import com.habitrpg.android.habitica.models.inventory.HatchingPotion;
import com.habitrpg.android.habitica.models.inventory.Item;
import com.habitrpg.android.habitica.models.inventory.Equipment;
import com.habitrpg.android.habitica.models.inventory.Mount;
import com.habitrpg.android.habitica.models.inventory.Pet;
import com.habitrpg.android.habitica.models.inventory.Quest;
import com.habitrpg.android.habitica.models.inventory.QuestContent;
import com.habitrpg.android.habitica.models.responses.FeedResponse;
import com.habitrpg.android.habitica.models.user.Items;
import com.habitrpg.android.habitica.models.user.User;

import java.util.List;

import io.realm.RealmResults;
import rx.Observable;


public interface InventoryRepository extends ContentRepository {
    Observable<QuestContent> getQuestContent(String key);

    Observable<RealmResults<Equipment>> getItems(List<String> searchedKeys);

    long getArmoireRemainingCount();

    Observable<List<Equipment>> getInventoryBuyableGear();

    Observable<RealmResults<Equipment>> getOwnedEquipment(String type);
    Observable<RealmResults<Equipment>> getOwnedEquipment();

    Observable<? extends RealmResults<? extends Item>> getOwnedItems(String itemType);

    Observable<Equipment> getEquipment(String key);

    Observable<Equipment> openMysteryItem(String key);

    void saveEquipment(Equipment equipment);

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

    Observable<User> sellItem(User user, String type, String key);
    Observable<User> sellItem(User user, Item item);

    Observable<Items> equipGear(User user, String equipment, boolean asCostume);
    Observable<Items> equip(User user, String type, String key);

    Observable<FeedResponse> feedPet(Pet pet, Food food);

    Observable<Items> hatchPet(Egg egg, HatchingPotion hatchingPotion);

    Observable<Quest> inviteToQuest(QuestContent quest);
}
