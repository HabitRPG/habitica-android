package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.habitrpg.android.habitica.models.inventory.Food;
import com.habitrpg.android.habitica.models.inventory.Mount;
import com.habitrpg.android.habitica.models.inventory.Pet;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;

public class PetListDeserializer implements JsonDeserializer<RealmList<Pet>> {
    @Override
    public RealmList<Pet> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        RealmList<Pet> vals = new RealmList<>();
        if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();

            Realm realm = Realm.getDefaultInstance();
            List<Pet> existingItems = realm.copyFromRealm(realm.where(Pet.class).findAll());
            realm.close();

            for (Pet item : existingItems) {
                if (object.has(item.getKey())) {
                    JsonElement itemObject = object.get(item.getKey());
                    item.setTrained(itemObject.getAsInt());
                    vals.add(item);
                    object.remove(item.getKey());
                }
            }

            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                Pet pet;
                if (entry.getValue().isJsonObject()) {
                    pet = context.deserialize(entry.getValue(), Food.class);
                } else {
                    pet = new Pet();
                    pet.setKey(entry.getKey());
                    if (entry.getValue().isJsonNull()) {
                        pet.setTrained(0);
                    } else {
                        pet.setTrained(entry.getValue().getAsInt());
                    }
                }
                vals.add(pet);
            }
        } else {
            for (JsonElement item : json.getAsJsonArray()) {
                vals.add(context.deserialize(item.getAsJsonObject(), Food.class));
            }
        }

        return vals;
    }
}