package com.magicmicky.habitrpgwrapper.lib.utils;

import android.annotation.SuppressLint;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.magicmicky.habitrpgwrapper.lib.models.Customization;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CustomizationDeserializer implements JsonDeserializer<List<Customization>> {

    @Override
    public List<Customization> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        List<Customization> customizations = new ArrayList<Customization>();


        if (object.has("shirt")) {
            List<Customization> existingCustomizations = new Select().from(Customization.class).where(Condition.column("type").isNot("background")).queryList();

            for (Customization customization : existingCustomizations) {
                if(object.has(customization.getType())) {
                    JsonObject nestedObject = object.get(customization.getType()).getAsJsonObject();
                    if (customization.getCategory() != null) {
                        if (nestedObject.has(customization.getCategory())) {
                            nestedObject = nestedObject.get(customization.getCategory()).getAsJsonObject();
                        } else {
                            continue;
                        }
                    }
                    if (nestedObject.has(customization.getIdentifier())) {
                        customizations.add(this.parseCustomization(customization, customization.getType(), customization.getCategory(), customization.getIdentifier(), nestedObject.get(customization.getIdentifier()).getAsJsonObject()));
                        nestedObject.remove(customization.getIdentifier());
                    }
                }
            }

            for (String type : Arrays.asList("shirt", "skin")) {
                for (Map.Entry<String, JsonElement> entry : object.get(type).getAsJsonObject().entrySet()) {
                    customizations.add(this.parseCustomization(null, type, null, entry.getKey(), entry.getValue().getAsJsonObject()));
                }
            }

            for (Map.Entry<String, JsonElement> categoryEntry : object.get("hair").getAsJsonObject().entrySet()) {
                for (Map.Entry<String, JsonElement> entry : categoryEntry.getValue().getAsJsonObject().entrySet()) {
                    customizations.add(this.parseCustomization(null, "hair", categoryEntry.getKey(), entry.getKey(), entry.getValue().getAsJsonObject()));
                }
            }
        } else {

            List<Customization> existingCustomizations = new Select().from(Customization.class).where(Condition.column("type").isNot("background")).queryList();

            for (Customization customization : existingCustomizations) {
                if(object.has(customization.getType())) {
                    JsonObject nestedObject = object.get(customization.getType()).getAsJsonObject();
                    if (customization.getCustomizationSet() != null) {
                        if (nestedObject.has(customization.getCustomizationSet())) {
                            nestedObject = nestedObject.get(customization.getCustomizationSet()).getAsJsonObject();
                        } else {
                            continue;
                        }
                    }
                    if (nestedObject.has(customization.getIdentifier())) {
                        customizations.add(this.parseBackground(customization, customization.getCustomizationSet(), customization.getIdentifier(), nestedObject.get(customization.getIdentifier()).getAsJsonObject()));
                        nestedObject.remove(customization.getIdentifier());
                    }
                }
            }

            for (Map.Entry<String, JsonElement> setEntry : object.entrySet()) {
                for (Map.Entry<String, JsonElement> entry : setEntry.getValue().getAsJsonObject().entrySet()) {
                    customizations.add(this.parseBackground(null, setEntry.getKey(), entry.getKey(), entry.getValue().getAsJsonObject()));
                }
            }
        }

        return customizations;
    }

    private Customization parseCustomization(Customization existingCustomizaion, String type, String category, String key, JsonObject entry) {
        JsonObject obj = entry;
        Customization customization = existingCustomizaion;
        if (customization == null) {
            customization = new Customization();
            customization.setIdentifier(key);
            customization.setType(type);
            if (category != null) {
                customization.setCategory(category);
            }
        }
        if (obj.has("price")) {
            customization.setPrice(obj.get("price").getAsInt());
        }

        if (obj.has("set")) {
            JsonObject setInfo = obj.get("set").getAsJsonObject();
            customization.setCustomizationSet(setInfo.get("key").getAsString());
            if (setInfo.has("setPrice")) {
                customization.setSetPrice(setInfo.get("setPrice").getAsInt());
            }
            if (setInfo.has("text")) {
                customization.setCustomizationSetName(setInfo.get("text").getAsString());
            }
            @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            try {
                if (setInfo.has("availableFrom")) {
                    customization.setAvailableFrom(format.parse(setInfo.get("availableFrom").getAsString()));
                }
                if (setInfo.has("availableUntil")) {
                    customization.setAvailableUntil(format.parse(setInfo.get("availableUntil").getAsString()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return customization;
    }

    private Customization parseBackground(Customization existingCustomization, String setName, String key, JsonObject entry) {
        Customization customization = existingCustomization;

        if (customization == null) {
            customization = new Customization();
            customization.setCustomizationSet(setName);
            String readableSetName = setName.substring(13, 17) + "." + setName.substring(11, 13);
            customization.setCustomizationSetName(readableSetName);
            customization.setType("background");
            customization.setIdentifier(key);
        }

        customization.setText(entry.get("text").getAsString());
        customization.setNotes(entry.getAsJsonObject().get("notes").getAsString());
        customization.setPrice(7);
        customization.setSetPrice(15);

        return customization;
    }
}
