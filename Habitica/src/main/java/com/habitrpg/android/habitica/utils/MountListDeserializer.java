package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.habitrpg.android.habitica.models.inventory.Mount;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class MountListDeserializer implements JsonDeserializer<HashMap<String, Mount>> {
    @Override
    public HashMap<String, Mount> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        HashMap<String, Mount> vals = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
            Mount pet = new Mount();
            pet.setKey(entry.getKey());
            pet.setAnimal(entry.getKey().split("-")[0]);
            pet.setColor(entry.getKey().split("-")[1]);
            vals.put(entry.getKey(), pet);
        }

        return vals;
    }
}
