package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.habitrpg.android.habitica.models.inventory.QuestDropItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;

/**
 * Created by phillip on 25.07.17.
 */

public class QuestDropItemsListSerialization implements JsonDeserializer<RealmList<QuestDropItem>> {
    @Override
    public RealmList<QuestDropItem> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        RealmList<QuestDropItem> items = new RealmList<>();
        List<String> keys = new ArrayList<>();

        for (JsonElement e : json.getAsJsonArray()) {
            QuestDropItem item = context.deserialize(e, QuestDropItem.class);
            if (keys.contains(item.getKey())) {
                for (QuestDropItem existingItem : items) {
                    if (existingItem.getKey().equals(item.getKey())) {
                        existingItem.setCount(existingItem.getCount()+1);
                    }
                }
            } else {
                item.setCount(1);
                items.add(item);
                keys.add(item.getKey());
            }
        }


        return items;
    }
}
