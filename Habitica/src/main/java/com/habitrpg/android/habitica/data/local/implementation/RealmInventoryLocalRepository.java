package com.habitrpg.android.habitica.data.local.implementation;

import android.content.Context;

import com.habitrpg.android.habitica.data.local.InventoryLocalRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.inventory.Egg;
import com.habitrpg.android.habitica.models.inventory.Equipment;
import com.habitrpg.android.habitica.models.inventory.Food;
import com.habitrpg.android.habitica.models.inventory.HatchingPotion;
import com.habitrpg.android.habitica.models.inventory.Item;
import com.habitrpg.android.habitica.models.inventory.Mount;
import com.habitrpg.android.habitica.models.inventory.Pet;
import com.habitrpg.android.habitica.models.inventory.QuestContent;
import com.habitrpg.android.habitica.models.inventory.SpecialItem;
import com.habitrpg.android.habitica.models.shops.ShopItem;
import com.habitrpg.android.habitica.models.user.User;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;


public class RealmInventoryLocalRepository extends RealmContentLocalRepository implements InventoryLocalRepository {
    private final Context context;

    public RealmInventoryLocalRepository(Realm realm, Context context) {
        super(realm);
        this.context = context;
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
                .in("key", searchedKeys.toArray(new String[0]))
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public long getArmoireRemainingCount() {
        return realm.where(Equipment.class)
                .equalTo("klass", "armoire")
                .beginGroup()
                .equalTo("owned", false)
                .or()
                .isNull("owned")
                .endGroup()
                .count();
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
    public Observable<? extends RealmResults<? extends Item>> getOwnedItems(String itemType, User user) {
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
                break;
            case "special":
                itemClass = SpecialItem.class;
                break;
        }
        if (itemClass == null) {
            return Observable.empty();
        }

        RealmQuery<? extends Item> query = realm.where(itemClass);
        if ("special".equals(itemType)) {
            if (user != null && user.getPurchased() != null && user.getPurchased().getPlan() != null) {
                SpecialItem mysticItem;
                if (query.count() == 0) {
                    mysticItem = SpecialItem.makeMysteryItem(context);
                } else {
                    mysticItem = getUnmanagedCopy((SpecialItem) query.findFirst());
                }
                mysticItem.setOwned(user.getPurchased().getPlan().mysteryItemCount);
                this.save(mysticItem);
            }
        } else {
            query = query.greaterThan("owned", 0);
        }
        return query.findAllAsync().asObservable().filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<Equipment> getEquipment(String key) {
        return realm.where(Equipment.class)
                .equalTo("key", key)
                .findFirstAsync()
                .asObservable()
                .filter(realmObject -> realmObject.isLoaded())
                .cast(Equipment.class);
    }

    @Override
    public Observable<RealmResults<Mount>> getMounts() {
        return realm.where(Mount.class)
                .findAllSorted("animalGroup", Sort.ASCENDING, "animal", Sort.ASCENDING)
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Mount>> getMounts(String type, String group) {
        return realm.where(Mount.class)
                .equalTo("animalGroup", group)
                .equalTo("animal", type)
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Mount>> getOwnedMounts() {
        return realm.where(Mount.class)
                .equalTo("owned", true)
                .findAllSorted("animalGroup", Sort.ASCENDING, "animal", Sort.ASCENDING)
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Mount>> getOwnedMounts(String animalType, String animalGroup) {
        if (animalGroup == null) {
            animalGroup = "";
        }
        animalGroup = animalGroup.replace("pets", "mounts").replace("Pets", "Mounts");
        return realm.where(Mount.class)
                .equalTo("animalGroup", animalGroup)
                .equalTo("animal", animalType)
                .equalTo("owned", true)
                .findAllSorted("animal")
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Pet>> getPets() {
        return realm.where(Pet.class)
                .findAllSorted("animalGroup", Sort.ASCENDING, "animal", Sort.ASCENDING)
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Pet>> getPets(String type, String group) {
        return realm.where(Pet.class)
                .equalTo("animalGroup", group)
                .equalTo("animal", type)
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Pet>> getOwnedPets() {
        return realm.where(Pet.class)
                .greaterThan("trained", 0)
                .findAllSorted("animalGroup", Sort.ASCENDING, "animal", Sort.ASCENDING)
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Pet>> getOwnedPets(String type, String group) {
        return realm.where(Pet.class)
                .equalTo("animalGroup", group)
                .equalTo("animal", type)
                .greaterThan("trained", 0)
                .findAllSorted("animal")
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public void updateOwnedEquipment(User user) {

    }

    @Override
    public void changeOwnedCount(String type, String key, int amountToAdd) {
        this.getItem(type, key).first().subscribe(item -> changeOwnedCount(item, amountToAdd), RxErrorHandler.handleEmptyError());
    }

    @Override
    public void changeOwnedCount(Item item, Integer amountToAdd) {
        realm.executeTransaction(realm1 -> item.setOwned(item.getOwned()+amountToAdd));
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

    @Override
    public void decrementMysteryItemCount(User user) {
        SpecialItem item = realm.where(SpecialItem.class).equalTo("isMysteryItem", true).findFirst();
        if (item.isValid()) {
            realm.executeTransactionAsync(realm1 -> {
                item.setOwned(item.getOwned()-1);
                if (user.getPurchased() != null && user.getPurchased().getPlan() != null) {
                    user.getPurchased().getPlan().mysteryItemCount -= 1;
                }
            });
        }
    }

    @Override
    public Observable<RealmResults<ShopItem>> getInAppRewards() {
        return realm.where(ShopItem.class)
                .findAllAsync()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }
}
