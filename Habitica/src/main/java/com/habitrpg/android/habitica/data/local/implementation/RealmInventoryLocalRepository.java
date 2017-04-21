package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.InventoryLocalRepository;
import com.habitrpg.android.habitica.models.inventory.Egg;
import com.habitrpg.android.habitica.models.inventory.Equipment;
import com.habitrpg.android.habitica.models.inventory.Food;
import com.habitrpg.android.habitica.models.inventory.HatchingPotion;
import com.habitrpg.android.habitica.models.inventory.Item;
import com.habitrpg.android.habitica.models.inventory.Mount;
import com.habitrpg.android.habitica.models.inventory.Pet;
import com.habitrpg.android.habitica.models.inventory.QuestContent;
import com.habitrpg.android.habitica.models.user.User;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import rx.Observable;


public class RealmInventoryLocalRepository extends RealmBaseLocalRepository implements InventoryLocalRepository {
    public RealmInventoryLocalRepository(Realm realm) {
        super(realm);
    }

    @Override
    public Observable<QuestContent> getQuestContent(String key) {
        return realm.where(QuestContent.class).equalTo("key", key).findFirstAsync()
                .asObservable()
                .filter(realmObject -> realmObject.isLoaded())
                .cast(QuestContent.class);
    }

    @Override
    public Observable<RealmResults<Equipment>> getEquipment(List<String> searchedKeys) {
        return realm.where(Equipment.class)
                .in("key", (String[]) searchedKeys.toArray())
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public long getArmoireRemainingCount() {
        return 0;
    }

    @Override
    public Observable<RealmResults<Equipment>> getOwnedEquipment(String type) {
        return realm.where(Equipment.class)
                .equalTo("type", type)
                .equalTo("owned", true)
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Equipment>> getOwnedEquipment() {
        return realm.where(Equipment.class)
                .equalTo("owned", true)
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<? extends RealmResults<? extends Item>> getOwnedItems(String itemType) {
        Class<? extends Item> itemClass = null;
        switch (itemType) {
            case "eggs":
                itemClass = Egg.class;
                break;
            case "hatchingPotions":
                itemClass = HatchingPotion.class;
                break;
            case "food":
                itemClass = Food.class;
                break;
            case "quests":
                itemClass = QuestContent.class;
        }
        return realm.where(itemClass).findAllAsync().asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<Equipment> getEquipment(String key) {
        return realm.where(Equipment.class)
                .equalTo("key", key)
                .findFirstAsync()
                .asObservable()
                .filter(realmObject -> realmObject.isLoaded())
                .cast(Equipment.class);    }

    @Override
    public void saveEquipment(Equipment equipment) {
        realm.executeTransaction(realm1 -> realm1.copyToRealm(equipment));
    }

    @Override
    public Observable<RealmResults<Mount>> getMounts() {
        return realm.where(Mount.class).findAll()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Mount>> getMounts(String type, String group) {
        return realm.where(Mount.class)
                .equalTo("group", group)
                .equalTo("type", type)
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Pet>> getPets() {
        return realm.where(Pet.class).findAll()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Pet>> getPets(String type, String group) {
        return realm.where(Pet.class)
                .equalTo("group", group)
                .equalTo("type", type)
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public void updateOwnedEquipment(User user) {

    }

    @Override
    public void changeOwnedCount(String type, String key, int amountToAdd) {
    }

    @Override
    public Observable<Item> getItem(String type, String key) {
        Class<? extends RealmObject> itemClass = null;
        switch (type) {
            case "eggs":
                itemClass = Egg.class;
                break;
            case "hatchingPotions":
                itemClass = HatchingPotion.class;
                break;
            case "food":
                itemClass = Food.class;
                break;
            case "quests":
                itemClass = QuestContent.class;
        }
        return realm.where(itemClass).equalTo("key", key).findFirstAsync().asObservable()
                .filter(realmObject -> realmObject.isLoaded())
                .cast(Item.class);
    }
}
