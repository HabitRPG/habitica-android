package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.InventoryLocalRepository;
import com.habitrpg.android.habitica.models.inventory.Egg;
import com.habitrpg.android.habitica.models.inventory.Food;
import com.habitrpg.android.habitica.models.inventory.HatchingPotion;
import com.habitrpg.android.habitica.models.inventory.Item;
import com.habitrpg.android.habitica.models.inventory.ItemData;
import com.habitrpg.android.habitica.models.inventory.Mount;
import com.habitrpg.android.habitica.models.inventory.Pet;
import com.habitrpg.android.habitica.models.inventory.QuestBoss;
import com.habitrpg.android.habitica.models.inventory.QuestContent;
import com.habitrpg.android.habitica.models.user.HabitRPGUser;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.Observable;

public class DbFlowInventoryLocalRepository implements InventoryLocalRepository {
    @Override
    public void close() {

    }

    @Override
    public Observable<QuestContent> getQuestContent(String key) {
        return Observable.defer(() -> Observable.just(new Select().from(QuestContent.class).querySingle()))
                .map(questContent -> {
                    questContent.boss = new Select().from(QuestBoss.class).where(Condition.column(ItemData.UNIQUE_IDENTIFIER).eq(key)).querySingle();
                    return questContent;
                });
    }

    @Override
    public Observable<List<ItemData>> getItems(List<String> searchedKeys) {
        Condition.In keyCondition = Condition.column(ItemData.UNIQUE_IDENTIFIER).in("");
        for (String item : searchedKeys) {
            keyCondition = keyCondition.and(item);
        }
        Where<ItemData> query = new Select().from(ItemData.class).where(keyCondition);
        return Observable.defer(() -> Observable.just(query.queryList()));
    }

    @Override
    public long getArmoireRemainingCount() {
        return new Select().count()
                .from(ItemData.class)
                .where(Condition.CombinedCondition.begin(Condition.column("klass").eq("armoire"))
                        .and(Condition.column("owned").isNull())
                ).count();
    }

    @Override
    public Observable<List<ItemData>> getOwnedEquipment(String type) {
        return Observable.defer(() -> Observable.just(new Select()
                .from(ItemData.class)
                .where(Condition.CombinedCondition.begin(Condition.column("type").eq(type))
                        .and(Condition.column("owned").eq(true))
                ).queryList()));
    }

    @Override
    public Observable<List<ItemData>> getOwnedEquipment() {
        return Observable.defer(() -> Observable.just(new Select().from(ItemData.class).where(Condition.column("owned").eq(true)).queryList()));
    }

    @Override
    public Observable<List<Item>> getOwnedItems(String itemType) {
        return Observable.defer(() -> {
                From from = null;
                switch (itemType) {
                    case "eggs":
                        from = new Select().from(Egg.class);
                        break;
                    case "hatchingPotions":
                        from = new Select().from(HatchingPotion.class);
                        break;
                    case "food":
                        from = new Select().from(Food.class);
                        break;
                    case "quests":
                        from = new Select().from(QuestContent.class);
                }
            if (from != null) {
                return Observable.just(((From<Item>)from).where(Condition.column("owned").greaterThan(0)).queryList());
            } else {
                return Observable.just(new ArrayList<Item>());
            }
        });
    }

    @Override
    public Observable<ItemData> getEquipment(String key) {
        return Observable.defer(() -> Observable.just(new Select().from(ItemData.class).where(Condition.column("key").eq(key)).querySingle()));
    }

    @Override
    public void saveEquipment(ItemData itemData) {
        itemData.async().save();
    }

    @Override
    public Observable<List<Mount>> getMounts() {
        return Observable.defer(() -> Observable.just(new Select().from(Mount.class).orderBy(true, "color").queryList()));
    }

    @Override
    public Observable<List<Mount>> getMounts(String type, String group) {
        return Observable.defer(() -> Observable.just(new Select()
                .from(Mount.class)
                .where(Condition.CombinedCondition
                        .begin(Condition.column("animal").eq(type))
                        .and(Condition.column("animalGroup").eq(group)))
                .orderBy(true, "color").queryList()));
    }

    @Override
    public Observable<List<Pet>> getPets() {
        return Observable.defer(() -> Observable.just(new Select().from(Pet.class).orderBy(true, "color").queryList()));
    }

    @Override
    public Observable<List<Pet>> getPets(String type, String group) {
        return Observable.defer(() -> Observable.just(new Select()
                .from(Pet.class)
                .where(Condition.CombinedCondition
                        .begin(Condition.column("animal").eq(type))
                        .and(Condition.column("animalGroup").eq(group)))
                .orderBy(true, "color").queryList()));
    }

    @Override
    public void updateOwnedEquipment(HabitRPGUser user) {
        if (user == null || user.getItems() == null) {
            return;
        }
        List<BaseModel> updates = new ArrayList<>();
        updates.addAll(this.updateOwnedData(Egg.class, user.getItems().getEggs()));
        updates.addAll(this.updateOwnedData(Food.class, user.getItems().getFood()));
        updates.addAll(this.updateOwnedData(HatchingPotion.class, user.getItems().getHatchingPotions()));
        updates.addAll(this.updateOwnedData(QuestContent.class, user.getItems().getQuests()));

        updates.addAll(this.updateOwnedData(user.getItems().getGear().owned));
        if (!updates.isEmpty()) {
            TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(updates)));
        }
    }

    @Override
    public void changeOwnedCount(String type, String key, int amountToAdd) {
        From from = null;

        switch (type) {
            case "hatchingpotion":
                from = new Select().from(HatchingPotion.class);
                break;
            case "food":
                from = new Select().from(Food.class);
                break;
            case "egg":
                from = new Select().from(Egg.class);
                break;
        }

        if (from != null) {
            from.where(Condition.column("key").eq(key))
                    .async()
                    .querySingle(new TransactionListener() {
                        @Override
                        public void onResultReceived(Object result) {
                            if (result != null) {
                                Item item = (Item) result;
                                item.setOwned(item.getOwned() + 1);
                                item.save();
                            }
                        }

                        @Override
                        public boolean onReady(BaseTransaction transaction) {
                            return true;
                        }

                        @Override
                        public boolean hasResult(BaseTransaction transaction, Object result) {
                            return true;
                        }
                    });
        }
    }

    private <T extends Item> List<T> updateOwnedData(Class<T> itemClass, HashMap<String, Integer> ownedMapping) {
        List<T> updates = new ArrayList<>();
        if (ownedMapping == null) {
            return updates;
        }
        List<T> items = new Select().from(itemClass).queryList();
        for (T item : items) {
            if (ownedMapping.containsKey(item.getKey()) && !item.getOwned().equals(ownedMapping.get(item.getKey()))) {
                item.setOwned(ownedMapping.get(item.getKey()));
                updates.add(item);
            } else if (!ownedMapping.containsKey(item.getKey()) && item.getOwned() > 0) {
                item.setOwned(0);
                updates.add(item);
            }
        }
        return updates;
    }

    private List<ItemData> updateOwnedData(HashMap<String, Boolean> ownedMapping) {
        List<ItemData> updates = new ArrayList<>();
        if (ownedMapping == null) {
            return updates;
        }
        List<ItemData> items = new Select().from(ItemData.class).queryList();
        for (ItemData item : items) {
            if (ownedMapping.containsKey(item.key) && item.owned != ownedMapping.get(item.key)) {
                item.owned = ownedMapping.get(item.key);
                updates.add(item);
            } else if (!ownedMapping.containsKey(item.key) && item.owned != null) {
                item.owned = null;
                updates.add(item);
            }
        }
        return updates;
    }
}
