package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.habitrpg.android.habitica.models.inventory.Pet;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;

public class PetMapDeserializer implements JsonDeserializer<HashMap<String, Pet>> {
    @Override
    public HashMap<String, Pet> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        HashMap<String, Pet> vals = new HashMap<>();
        if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();

            Realm realm = Realm.getDefaultInstance();
            List<Pet> existingItems = realm.copyFromRealm(realm.where(Pet.class).findAll());
            realm.close();

            for (Pet pet : existingItems) {
                if (object.has(pet.getKey())) {
                    vals.put(pet.getKey(), pet);
                    object.remove(pet.getKey());
                }
            }

            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                Pet pet;
                pet = new Pet();
                pet.setKey(entry.getKey());
                pet.setAnimal(entry.getKey().split("-")[0]);
                pet.setColor(entry.getKey().split("-")[1]);
                vals.put(pet.getKey(), pet);
            }
        }

        return vals;
    }
}
