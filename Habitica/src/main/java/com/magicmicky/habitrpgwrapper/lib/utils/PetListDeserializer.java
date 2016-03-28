package com.magicmicky.habitrpgwrapper.lib.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.HatchingPotion;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Pet;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PetListDeserializer implements JsonDeserializer<List<Pet>> {
    @Override
    public List<Pet> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<Pet> vals = new ArrayList<>();
        for (Map.Entry<String,JsonElement> entry : json.getAsJsonObject().entrySet()) {
            Pet pet = new Pet();
            pet.setKey(entry.getKey());
            pet.setAnimal(entry.getKey().split("-")[0]);
            pet.setColor(entry.getKey().split("-")[1]);
            vals.add(pet);
        }

        return vals;
    }
}
