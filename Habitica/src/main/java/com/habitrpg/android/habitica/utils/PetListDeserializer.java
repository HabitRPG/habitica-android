package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.habitrpg.android.habitica.models.inventory.Food;
import com.habitrpg.android.habitica.models.inventory.Pet;

import java.lang.reflect.Type;
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
                    try {
                        item.setTrained(itemObject.getAsInt());
                    } catch (UnsupportedOperationException ignored) {
                    }
                    vals.add(item);
                    object.remove(item.getKey());
                }
            }

            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                Pet pet = new Pet();
                pet.setKey(entry.getKey());
                pet.setAnimal(entry.getKey().split("-")[0]);
                pet.setColor(entry.getKey().split("-")[1]);
                if (entry.getValue().isJsonNull()) {
                    pet.setTrained(0);
                } else {
                    try {
                        pet.setTrained(entry.getValue().getAsInt());
                    } catch (UnsupportedOperationException ignored) {
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