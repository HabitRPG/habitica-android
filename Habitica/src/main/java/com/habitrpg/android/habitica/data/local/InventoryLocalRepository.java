package com.habitrpg.android.habitica.data.local;

import com.habitrpg.android.habitica.models.inventory.Item;
import com.habitrpg.android.habitica.models.inventory.ItemData;
import com.habitrpg.android.habitica.models.inventory.Mount;
import com.habitrpg.android.habitica.models.inventory.Pet;
import com.habitrpg.android.habitica.models.inventory.QuestContent;
import com.habitrpg.android.habitica.models.user.HabitRPGUser;

import java.util.List;

import rx.Observable;

public interface InventoryLocalRepository extends BaseLocalRepository {
    Observable<QuestContent> getQuestContent(String key);

    Observable<List<ItemData>> getItems(List<String> searchedKeys);

    long getArmoireRemainingCount();

    Observable<List<ItemData>> getOwnedEquipment(String type);
    Observable<List<ItemData>> getOwnedEquipment();

    Observable<List<Item>> getOwnedItems(String itemType);

    Observable<ItemData> getEquipment(String key);

    void saveEquipment(ItemData itemData);

    Observable<List<Mount>> getMounts();
    Observable<List<Mount>> getMounts(String type, String group);

    Observable<List<Pet>> getPets();
    Observable<List<Pet>> getPets(String type, String group);

    void updateOwnedEquipment(HabitRPGUser user);

    void changeOwnedCount(String type, String key, int amountToAdd);
}
