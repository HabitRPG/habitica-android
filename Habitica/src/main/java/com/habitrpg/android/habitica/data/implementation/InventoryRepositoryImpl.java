package com.habitrpg.android.habitica.data.implementation;

import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.InventoryRepository;
import com.habitrpg.android.habitica.data.local.InventoryLocalRepository;
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
import com.habitrpg.android.habitica.models.user.Outfit;
import com.habitrpg.android.habitica.models.user.Stats;
import com.habitrpg.android.habitica.models.user.User;

import java.util.List;

import io.realm.RealmResults;
import rx.Observable;

public class InventoryRepositoryImpl extends ContentRepositoryImpl<InventoryLocalRepository> implements InventoryRepository {

    public InventoryRepositoryImpl(InventoryLocalRepository localRepository, ApiClient apiClient) {
        super(localRepository, apiClient);
    }

    @Override
    public Observable<QuestContent> getQuestContent(String key) {
        return localRepository.getQuestContent(key);
    }

    @Override
    public Observable<RealmResults<Equipment>> getItems(List<String> searchedKeys) {
        return localRepository.getEquipment(searchedKeys);
    }

    @Override
    public long getArmoireRemainingCount() {
        return localRepository.getArmoireRemainingCount();
    }

    @Override
    public Observable<List<Equipment>> getInventoryBuyableGear() {
        return apiClient.getInventoryBuyableGear();
    }

    @Override
    public Observable<RealmResults<Equipment>> getOwnedEquipment(String type) {
        return localRepository.getOwnedEquipment(type);
    }

    @Override
    public Observable<RealmResults<Equipment>> getOwnedEquipment() {
        return localRepository.getOwnedEquipment();
    }

    @Override
    public Observable<? extends RealmResults<? extends Item>> getOwnedItems(String itemType) {
        return localRepository.getOwnedItems(itemType);
    }

    @Override
    public Observable<Equipment> getEquipment(String key) {
        return localRepository.getEquipment(key);
    }

    @Override
    public Observable<Equipment> openMysteryItem(String key) {
        return apiClient.openMysteryItem().doOnNext(itemData -> {
            itemData.setOwned(true);
            localRepository.saveEquipment(itemData);
        });
    }

    @Override
    public void saveEquipment(Equipment equipment) {
        localRepository.saveEquipment(equipment);
    }

    @Override
    public Observable<RealmResults<Mount>> getMounts() {
        return localRepository.getMounts();
    }

    @Override
    public Observable<RealmResults<Mount>> getMounts(String type, String group) {
        return localRepository.getMounts(type, group);
    }

    @Override
    public Observable<RealmResults<Mount>> getOwnedMounts() {
        return localRepository.getOwnedMounts();
    }

    @Override
    public Observable<RealmResults<Mount>> getOwnedMounts(String animalType, String animalGroup) {
        return localRepository.getOwnedMounts(animalType, animalGroup);
    }

    @Override
    public Observable<RealmResults<Pet>> getPets() {
        return localRepository.getPets();
    }

    @Override
    public Observable<RealmResults<Pet>> getPets(String type, String group) {
        return localRepository.getPets(type, group);
    }

    @Override
    public Observable<RealmResults<Pet>> getOwnedPets() {
        return localRepository.getOwnedPets();
    }

    @Override
    public Observable<RealmResults<Pet>> getOwnedPets(String type, String group) {
        return localRepository.getOwnedPets(type, group);
    }

    @Override
    public void updateOwnedEquipment(User user) {
        localRepository.updateOwnedEquipment(user);
    }

    @Override
    public void changeOwnedCount(String type, String key, int amountToAdd) {
        localRepository.changeOwnedCount(type, key, amountToAdd);
    }

    @Override
    public Observable<User> sellItem(User user, String type, String key) {
        return localRepository.getItem(type, key)
                .flatMap(item -> sellItem(user, item));
    }

    @Override
    public Observable<User> sellItem(User user, Item item) {
        return apiClient.sellItem(item.getType(), item.getKey())
                .map(user1 -> {
                    localRepository.executeTransaction(realm -> {
                        if (user != null) {
                            user1.getItems().setUserId(user.getId());
                            Items items = realm.copyToRealmOrUpdate(user1.getItems());
                            user1.getStats().setUserId(user.getId());
                            Stats stats = realm.copyToRealmOrUpdate(user1.getStats());
                            user.setItems(items);
                            user.setStats(stats);
                            item.setOwned(item.getOwned()-1);
                        }
                    });
                    return user;
                });
    }

    @Override
    public Observable<Items> equipGear(User user, String key, boolean asCostume) {
        return equip(user, asCostume ? "costume" : "equipped", key);
    }

    @Override
    public Observable<Items> equip(User user, String type, String key) {
        return apiClient.equipItem(type, key)
                .doOnNext(items -> {
                    if (user == null) {
                        return;
                    }
                    localRepository.executeTransaction(realm -> {
                        Outfit newEquipped = items.getGear().getEquipped();
                        Outfit oldEquipped = user.getItems().getGear().getEquipped();
                        Outfit newCostume = items.getGear().getCostume();
                        Outfit oldCostume = user.getItems().getGear().getCostume();
                        oldEquipped.updateWith(newEquipped);
                        oldCostume.updateWith(newCostume);
                        user.getItems().setCurrentMount(items.getCurrentMount());
                        user.getItems().setCurrentPet(items.getCurrentPet());
                        user.setBalance(user.getBalance());
                    });
                });
    }

    @Override
    public Observable<FeedResponse> feedPet(Pet pet, Food food) {
        return apiClient.feedPet(pet.getKey(), food.getKey())
                .doOnNext(feedResponse -> {
                    localRepository.changeOwnedCount(food, -1);
                    localRepository.executeTransaction(realm -> pet.setTrained(feedResponse.value));
                });
    }

    @Override
    public Observable<Items> hatchPet(Egg egg, HatchingPotion hatchingPotion) {
        return apiClient.hatchPet(egg.getKey(), hatchingPotion.getKey())
                .doOnNext(items -> {
                    localRepository.changeOwnedCount(egg, -1);
                    localRepository.changeOwnedCount(hatchingPotion, -1);
                });
    }

    @Override
    public Observable<Quest> inviteToQuest(QuestContent quest) {
        return apiClient.inviteToQuest("party", quest.getKey())
                .doOnNext(quest1 -> localRepository.changeOwnedCount(quest, -1));
    }
}
