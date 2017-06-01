package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import com.habitrpg.android.habitica.models.inventory.Pet;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class PetListDeserializer implements JsonDeserializer<HashMap<String, Pet>> {
    @Override
    public HashMap<String, Pet> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        HashMap<String, Pet> vals = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
            Pet pet = new Pet();
            pet.setKey(entry.getKey());
            pet.setAnimal(entry.getKey().split("-")[0]);
            pet.setColor(entry.getKey().split("-")[1]);
            vals.put(entry.getKey(), pet);
        }

        return vals;
    }
}
