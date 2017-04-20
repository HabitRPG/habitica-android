package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.InventoryLocalRepository;
import com.habitrpg.android.habitica.models.inventory.Equipment;
import com.habitrpg.android.habitica.models.inventory.Item;
import com.habitrpg.android.habitica.models.inventory.Mount;
import com.habitrpg.android.habitica.models.inventory.Pet;
import com.habitrpg.android.habitica.models.inventory.QuestContent;
import com.habitrpg.android.habitica.models.user.User;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
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
    public Observable<RealmResults<Equipment>> getItems(List<String> searchedKeys) {
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
    public Observable<List<Item>> getOwnedItems(String itemType) {
        return null;
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
}
