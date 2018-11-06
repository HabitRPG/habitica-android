package com.habitrpg.android.habitica.utils;

import android.annotation.SuppressLint;
import androidx.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.habitrpg.android.habitica.models.inventory.Customization;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;

public class CustomizationDeserializer implements JsonDeserializer<List<Customization>> {

    @Override
    public List<Customization> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        RealmList<Customization> customizations = new RealmList<>();
        Realm realm = Realm.getDefaultInstance();

        if (object.has("shirt")) {
            List<Customization> existingCustomizations = realm.copyFromRealm(realm.where(Customization.class).findAll());

            for (Customization customization : existingCustomizations) {
                if (object.has(customization.getType())) {
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

            for (String type : Arrays.asList("shirt", "skin", "chair")) {
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
            List<Customization> existingCustomizations = realm.copyFromRealm(realm.where(Customization.class).findAll());

            for (Customization customization : existingCustomizations) {
                if (object.has(customization.getCustomizationSet())) {
                    JsonObject nestedObject = object.get(customization.getCustomizationSet()).getAsJsonObject();
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

        realm.close();

        return customizations;
    }

    private Customization parseCustomization(@Nullable Customization existingCustomizaion, String type, @Nullable String category, String key, JsonObject entry) {
        Customization customization = existingCustomizaion;
        if (customization == null) {
            customization = new Customization();
            customization.setIdentifier(key);
            customization.setType(type);
            if (category != null) {
                customization.setCategory(category);
            }
        }
        if (entry.has("price")) {
            customization.setPrice(entry.get("price").getAsInt());
        }

        if (entry.has("set")) {
            JsonObject setInfo = entry.get("set").getAsJsonObject();
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

    private Customization parseBackground(@Nullable Customization existingCustomization, String setName, String key, JsonObject entry) {
        Customization customization = existingCustomization;

        if (customization == null) {
            customization = new Customization();
            customization.setCustomizationSet(setName);
            String readableSetName = setName.substring(13, 17) + "." + setName.substring(11, 13);
            customization.setCustomizationSetName(readableSetName);
            customization.setType("background");
            customization.setIdentifier(key);
        }
        if ("incentiveBackgrounds".equals(setName)) {
            customization.setCustomizationSetName("Login Incentive");
            customization.setPrice(0);
            customization.setSetPrice(0);
            customization.setIsBuyable(false);
        } else {
            customization.setPrice(7);
            customization.setSetPrice(15);
        }

        customization.setText(entry.get("text").getAsString());
        customization.setNotes(entry.getAsJsonObject().get("notes").getAsString());

        return customization;
    }
}
