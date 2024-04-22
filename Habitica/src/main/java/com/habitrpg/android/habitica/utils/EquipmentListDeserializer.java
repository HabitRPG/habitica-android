package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.habitrpg.android.habitica.models.inventory.Equipment;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;

public class EquipmentListDeserializer implements JsonDeserializer<List<Equipment>> {
    @Override
    public List<Equipment> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        RealmList<Equipment> vals = new RealmList<>();
        if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();

            Realm realm = Realm.getDefaultInstance();
            List<Equipment> existingItems = realm.copyFromRealm(realm.where(Equipment.class).findAll());
            realm.close();

            for (Equipment item : existingItems) {
                if (object.has(item.getKey())) {
                    JsonElement itemObject = object.get(item.getKey());

                    if (itemObject.isJsonObject()) {
                        Equipment parsedItem = context.deserialize(itemObject.getAsJsonObject(), Equipment.class);
                        item.setText(parsedItem.getText());
                        item.setValue(parsedItem.getValue());
                        item.setType(parsedItem.getType());
                        item.setKlass(parsedItem.getKlass());
                        item.setSpecialClass(parsedItem.getSpecialClass());
                        item.setIndex(parsedItem.getIndex());
                        item.setNotes(parsedItem.getNotes());
                        item.setCon(parsedItem.getCon());
                        item.setStr(parsedItem.getStr());
                        item.setPer(parsedItem.getPer());
                        item.setIntelligence(parsedItem.getIntelligence());
                        item.setTwoHanded(parsedItem.getTwoHanded());
                        item.setMystery(parsedItem.getMystery());
                        item.setGearSet(parsedItem.getGearSet());
                    } else {
                        item.setOwned(itemObject.getAsBoolean());
                    }
                    vals.add(item);
                    object.remove(item.getKey());
                }
            }

            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                Equipment item;
                if (entry.getValue().isJsonObject()) {
                    item = context.deserialize(entry.getValue(), Equipment.class);
                } else {
                    item = new Equipment();
                    item.setKey(entry.getKey());
                    if (entry.getValue().isJsonNull()) {
                        item.setOwned(false);
                    } else {
                        item.setOwned(entry.getValue().getAsBoolean());
                    }
                }
                vals.add(item);
            }
        } else {
            for (JsonElement item : json.getAsJsonArray()) {
                vals.add(context.deserialize(item.getAsJsonObject(), Equipment.class));
            }
        }

        return vals;
    }
}
