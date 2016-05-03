package com.magicmicky.habitrpgwrapper.lib.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Mount;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Pet;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MountListDeserializer implements JsonDeserializer<HashMap<String, Mount>> {
    @Override
    public HashMap<String, Mount> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        HashMap<String, Mount> vals = new HashMap<>();
        for (Map.Entry<String,JsonElement> entry : json.getAsJsonObject().entrySet()) {
            Mount pet = new Mount();
            pet.setKey(entry.getKey());
            pet.setAnimal(entry.getKey().split("-")[0]);
            pet.setColor(entry.getKey().split("-")[1]);
            vals.put(entry.getKey(), pet);
        }

        return vals;
    }
}
