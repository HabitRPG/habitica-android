package com.magicmicky.habitrpgwrapper.lib.utils;

import android.annotation.SuppressLint;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.magicmicky.habitrpgwrapper.lib.models.Customization;
import com.magicmicky.habitrpgwrapper.lib.models.Purchases;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ChecklistItem;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by viirus on 14/01/16.
 */
public class CustomizationDeserializer implements JsonDeserializer<List<Customization>> {

    @Override
    public List<Customization> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        List<Customization> customizations = new ArrayList<Customization>();

        if (object.has("shirt")) {
            for (String type : Arrays.asList("shirt", "skin")) {
                for (Map.Entry<String, JsonElement> entry : object.get(type).getAsJsonObject().entrySet()) {
                    customizations.add(this.parseCustomization(type, null, entry));
                }
            }

            for (Map.Entry<String, JsonElement> categoryEntry : object.get("hair").getAsJsonObject().entrySet()) {
                for (Map.Entry<String, JsonElement> entry : categoryEntry.getValue().getAsJsonObject().entrySet()) {
                    customizations.add(this.parseCustomization("hair", categoryEntry.getKey(), entry));
                }
            }
        } else {
            for (Map.Entry<String, JsonElement> setEntry : object.entrySet()) {
                for (Map.Entry<String, JsonElement> entry : setEntry.getValue().getAsJsonObject().entrySet()) {
                    customizations.add(this.parseBackground(setEntry.getKey(), entry));
                }
            }
        }
        TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(customizations)));

        return customizations;
    }

    private Customization parseCustomization(String type, String category, Map.Entry<String,JsonElement> entry) {
        JsonObject obj = entry.getValue().getAsJsonObject();
        Customization customization = new Customization();
        customization.setType(type);
        if (category != null) {
            customization.setCategory(category);
        }
        customization.setIdentifier(entry.getKey());
        if (obj.has("price")) {
            customization.setPrice(obj.get("price").getAsInt());
        }

        if (obj.has("set")) {
            JsonObject setInfo = obj.get("set").getAsJsonObject();
            customization.setCustomizationSet(setInfo.get("key").getAsString());
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

    private Customization parseBackground(String setName, Map.Entry<String,JsonElement> entry) {
        JsonObject obj = entry.getValue().getAsJsonObject();
        Customization customization = new Customization();
        customization.setCustomizationSet(setName);
        customization.setType("background");
        customization.setIdentifier(entry.getKey());
        customization.setText(entry.getValue().getAsJsonObject().get("text").getAsString());
        customization.setNotes(entry.getValue().getAsJsonObject().get("notes").getAsString());
        customization.setPrice(7);
        customization.setSetPrice(15);

        return customization;
    }
}
