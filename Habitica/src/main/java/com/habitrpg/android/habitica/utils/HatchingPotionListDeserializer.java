package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.habitrpg.android.habitica.models.inventory.HatchingPotion;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;

public class HatchingPotionListDeserializer implements JsonDeserializer<List<HatchingPotion>> {
    @Override
    public List<HatchingPotion> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        RealmList<HatchingPotion> vals = new RealmList<>();
        if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();

            Realm realm = Realm.getDefaultInstance();
            List<HatchingPotion> existingItems = realm.copyFromRealm(realm.where(HatchingPotion.class).findAll());
            realm.close();

            for (HatchingPotion item : existingItems) {
                if (object.has(item.getKey())) {
                    JsonElement itemObject = object.get(item.getKey());

                    if (itemObject.isJsonObject()) {
                        HatchingPotion parsedItem = context.deserialize(itemObject.getAsJsonObject(), HatchingPotion.class);
                        item.setText(parsedItem.getText());
                        item.setNotes(parsedItem.getNotes());
                        item.setValue(parsedItem.getValue());
                        item.setLimited(parsedItem.getLimited());
                        item.setPremium(parsedItem.getPremium());
                    } else {
                        item.setOwned(itemObject.getAsInt());
                    }
                    vals.add(item);
                    object.remove(item.getKey());
                }
            }

            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                HatchingPotion item;
                if (entry.getValue().isJsonObject()) {
                    item = context.deserialize(entry.getValue(), HatchingPotion.class);
                } else {
                    item = new HatchingPotion();
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
                vals.add(context.deserialize(item.getAsJsonObject(), HatchingPotion.class));
            }
        }

        return vals;
    }
}
