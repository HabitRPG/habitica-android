package com.habitrpg.android.habitica.data;

import com.habitrpg.android.habitica.models.inventory.Egg;
import com.habitrpg.android.habitica.models.inventory.Equipment;
import com.habitrpg.android.habitica.models.inventory.Food;
import com.habitrpg.android.habitica.models.inventory.HatchingPotion;
import com.habitrpg.android.habitica.models.inventory.Item;
import com.habitrpg.android.habitica.models.inventory.Mount;
import com.habitrpg.android.habitica.models.inventory.Pet;
import com.habitrpg.android.habitica.models.inventory.Quest;
import com.habitrpg.android.habitica.models.inventory.QuestContent;
import com.habitrpg.android.habitica.models.responses.BuyResponse;
import com.habitrpg.android.habitica.models.responses.FeedResponse;
import com.habitrpg.android.habitica.models.shops.Shop;
import com.habitrpg.android.habitica.models.shops.ShopItem;
import com.habitrpg.android.habitica.models.user.Items;
import com.habitrpg.android.habitica.models.user.User;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.realm.RealmResults;


public interface InventoryRepository extends ContentRepository {
    Flowable<QuestContent> getQuestContent(String key);

    Flowable<RealmResults<Equipment>> getItems(List<String> searchedKeys);

    long getArmoireRemainingCount();

    Flowable<RealmResults<ShopItem>> getInAppRewards();
    Flowable<List<ShopItem>> retrieveInAppRewards();

    Flowable<RealmResults<Equipment>> getOwnedEquipment(@NotNull String type);
    Flowable<RealmResults<Equipment>> getOwnedEquipment();

    Flowable<? extends RealmResults<? extends Item>> getOwnedItems(Class<? extends Item> itemClass, User user);
    Flowable<? extends Map<String, Item>> getOwnedItems(@NotNull User user);

    Flowable<Equipment> getEquipment(String key);

    Flowable<Equipment> openMysteryItem(User user);

    void saveEquipment(Equipment equipment);

    Flowable<RealmResults<Mount>> getMounts();
    Flowable<RealmResults<Mount>> getMounts(@NotNull String type, @NotNull String group);

    Flowable<RealmResults<Mount>> getOwnedMounts();
    Flowable<RealmResults<Mount>> getOwnedMounts(@NotNull String animalType, @NotNull String animalGroup);

    Flowable<RealmResults<Pet>> getPets();
    Flowable<RealmResults<Pet>> getPets(@NotNull String type, @NotNull String group);

    Flowable<RealmResults<Pet>> getOwnedPets();
    Flowable<RealmResults<Pet>> getOwnedPets(@NotNull String type, @NotNull String group);

    void updateOwnedEquipment(User user);

    void changeOwnedCount(String type, String key, int amountToAdd);

    Flowable<User> sellItem(User user, String type, String key);
    Flowable<User> sellItem(User user, Item item);

    Flowable<Items> equipGear(User user, String equipment, boolean asCostume);
    Flowable<Items> equip(User user, String type, String key);

    Flowable<FeedResponse> feedPet(Pet pet, Food food);

    Flowable<Items> hatchPet(Egg egg, HatchingPotion hatchingPotion);

    Flowable<Quest> inviteToQuest(QuestContent quest);

    Flowable<BuyResponse> buyItem(User user, String id, double value);

    Flowable<Shop> retrieveShopInventory(String identifier);
    Flowable<Shop> retrieveMarketGear();

    Flowable<Void> purchaseMysterySet(String categoryIdentifier);

    Flowable<Void> purchaseHourglassItem(String purchaseType, String key);

    Flowable<Void> purchaseQuest(String key);

    Flowable<Void> purchaseItem(String purchaseType, String key);

    Flowable<List<ShopItem>> togglePinnedItem(ShopItem item);
}
