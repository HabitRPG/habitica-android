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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.OrderedRealmCollectionSnapshot;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import rx.Observable;
import rx.functions.Func4;


public class RealmInventoryLocalRepository extends RealmContentLocalRepository implements InventoryLocalRepository {
    private final Context context;

    public RealmInventoryLocalRepository(Realm realm, Context context) {
        super(realm);
        this.context = context;
    }

    @Override
    public Observable<QuestContent> getQuestContent(String key) {
        return getRealm().where(QuestContent.class).equalTo("key", key).findFirstAsync()
                .asObservable()
                .filter(realmObject -> realmObject.isLoaded())
                .cast(QuestContent.class);
    }

    @Override
    public Observable<RealmResults<Equipment>> getEquipment(List<String> searchedKeys) {
        return getRealm().where(Equipment.class)
                .in("key", searchedKeys.toArray(new String[0]))
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public long getArmoireRemainingCount() {
        return getRealm().where(Equipment.class)
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
        return getRealm().where(Equipment.class)
                .equalTo("type", type)
                .equalTo("owned", true)
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Equipment>> getOwnedEquipment() {
        return getRealm().where(Equipment.class)
                .equalTo("owned", true)
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<? extends RealmResults<? extends Item>> getOwnedItems(Class<? extends Item> itemClass, User user) {
        if (itemClass == null) {
            return Observable.empty();
        }

        RealmQuery<? extends Item> query = getRealm().where(itemClass);
        if (SpecialItem.class.isAssignableFrom(itemClass)) {
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
    public Observable<? extends Map<String, Item>> getOwnedItems(User user) {
        return Observable.combineLatest(
                getOwnedItems(Egg.class, user),
                getOwnedItems(HatchingPotion.class, user),
                getOwnedItems(Food.class, user),
                getOwnedItems(QuestContent.class, user),
                (Func4<RealmResults<? extends Item>, RealmResults<? extends Item>, RealmResults<? extends Item>, RealmResults<? extends Item>, Map<String, Item>>) (eggs, hatchingPotions, food, quests) -> {
                    Map<String, Item> items = new HashMap<>();
                    for (Item item : eggs) {
                        items.put(item.getKey()+"-"+item.getType(), item);
                    }
                    for (Item item : hatchingPotions) {
                        items.put(item.getKey()+"-"+item.getType(), item);
                    }
                    for (Item item : food) {
                        items.put(item.getKey()+"-"+item.getType(), item);
                    }
                    for (Item item : quests) {
                        items.put(item.getKey()+"-"+item.getType(), item);
                    }
                    return items;
                }
        );
    }

    @Override
    public Observable<Equipment> getEquipment(String key) {
        return getRealm().where(Equipment.class)
                .equalTo("key", key)
                .findFirstAsync()
                .asObservable()
                .filter(realmObject -> realmObject.isLoaded())
                .cast(Equipment.class);
    }

    @Override
    public Observable<RealmResults<Mount>> getMounts() {
        return getRealm().where(Mount.class)
                .findAllSorted("animalGroup", Sort.ASCENDING, "animal", Sort.ASCENDING)
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Mount>> getMounts(String type, String group) {
        return getRealm().where(Mount.class)
                .equalTo("animalGroup", group)
                .equalTo("animal", type)
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Mount>> getOwnedMounts() {
        return getRealm().where(Mount.class)
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
        return getRealm().where(Mount.class)
                .equalTo("animalGroup", animalGroup)
                .equalTo("animal", animalType)
                .equalTo("owned", true)
                .findAllSorted("animal")
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Pet>> getPets() {
        return getRealm().where(Pet.class)
                .findAllSorted("animalGroup", Sort.ASCENDING, "animal", Sort.ASCENDING)
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Pet>> getPets(String type, String group) {
        return getRealm().where(Pet.class)
                .equalTo("animalGroup", group)
                .equalTo("animal", type)
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Pet>> getOwnedPets() {
        return getRealm().where(Pet.class)
                .greaterThan("trained", 0)
                .findAllSorted("animalGroup", Sort.ASCENDING, "animal", Sort.ASCENDING)
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Pet>> getOwnedPets(String type, String group) {
        return getRealm().where(Pet.class)
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
        getRealm().executeTransaction(realm1 -> item.setOwned(item.getOwned()+amountToAdd));
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
        return getRealm().where(itemClass).equalTo("key", key).findFirstAsync().asObservable()
                .filter(realmObject -> realmObject.isLoaded())
                .cast(Item.class);
    }

    @Override
    public void decrementMysteryItemCount(User user) {
        SpecialItem item = getRealm().where(SpecialItem.class).equalTo("isMysteryItem", true).findFirst();
        getRealm().executeTransaction(realm1 -> {
            if (item != null && item.isValid()) {
                item.setOwned(item.getOwned() - 1);
            }
            if (user.isValid() && user.getPurchased() != null && user.getPurchased().getPlan() != null) {
                user.getPurchased().getPlan().mysteryItemCount -= 1;
            }
        });
    }

    @Override
    public Observable<RealmResults<ShopItem>> getInAppRewards() {
        return getRealm().where(ShopItem.class)
                .findAllAsync()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public void saveInAppRewards(@NotNull List<? extends ShopItem> onlineItems) {
        OrderedRealmCollectionSnapshot<ShopItem> localItems = getRealm().where(ShopItem.class).findAll().createSnapshot();
        getRealm().executeTransaction(realm1 -> {
            for (ShopItem localItem : localItems) {
                if (!onlineItems.contains(localItem)) {
                    localItem.deleteFromRealm();
                }
            }
            getRealm().insertOrUpdate(onlineItems);
        });
    }

    @Override
    public void changePetFeedStatus(@Nullable String key, int feedStatus) {
        Pet pet = getRealm().where(Pet.class).equalTo("key", key).findFirst();
        if (pet != null) {
            executeTransaction(realm -> { pet.setTrained(feedStatus); });
        }
    }
}
