package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.habitrpg.android.habitica.models.inventory.QuestContent;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;

public class QuestListDeserializer implements JsonDeserializer<List<QuestContent>> {
    @Override
    public List<QuestContent> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        RealmList<QuestContent> vals = new RealmList<>();
        if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();

            Realm realm = Realm.getDefaultInstance();
            List<QuestContent> existingItems = realm.copyFromRealm(realm.where(QuestContent.class).findAll());
            realm.close();

            for (QuestContent item : existingItems) {
                if (object.has(item.getKey())) {
                    JsonElement itemObject = object.get(item.getKey());

                    if (itemObject.isJsonObject()) {
                        QuestContent parsedItem = context.deserialize(itemObject.getAsJsonObject(), QuestContent.class);
                        item.setText(parsedItem.getText());
                        item.setNotes(parsedItem.getNotes());
                        item.setValue(parsedItem.getValue());
                        item.setPrevious(parsedItem.getPrevious());
                        item.setCanBuy(parsedItem.isCanBuy());
                        item.setBoss(parsedItem.getBoss());
                        if (item.getBoss() != null) {
                            item.getBoss().key = item.getKey();
                        }
                        item.setCategory(parsedItem.getCategory());
                        item.setCollect(parsedItem.getCollect());
                        item.setLvl(parsedItem.getLvl());
                    } else {
                        item.setOwned(itemObject.getAsInt());
                    }
                    vals.add(item);
                    object.remove(item.getKey());
                }
            }

            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                QuestContent item;
                if (entry.getValue().isJsonObject()) {
                    item = context.deserialize(entry.getValue(), QuestContent.class);
                } else {
                    item = new QuestContent();
                    item.setKey(entry.getKey());
                    if (entry.getValue().isJsonNull()) {
                        item.setOwned(0);
                    } else {
                        item.setOwned(entry.getValue().getAsInt());
                    }
                }
                vals.add(item);
            }
        } else {
            for (JsonElement item : json.getAsJsonArray()) {
                vals.add(context.deserialize(item.getAsJsonObject(), QuestContent.class));
            }
        }

        return vals;
    }
}
