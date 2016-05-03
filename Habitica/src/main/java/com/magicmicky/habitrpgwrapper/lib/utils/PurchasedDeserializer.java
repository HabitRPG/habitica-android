package com.magicmicky.habitrpgwrapper.lib.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.magicmicky.habitrpgwrapper.lib.models.Customization;
import com.magicmicky.habitrpgwrapper.lib.models.Purchases;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by viirus on 14/01/16.
 */
public class PurchasedDeserializer implements JsonDeserializer<Purchases> {

    @Override
    public Purchases deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        List<Customization> customizations = new ArrayList<Customization>();
        Purchases purchases = new Purchases();

        List<Customization> existingCustomizations = new Select().from(Customization.class).queryList();

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
                    customizations.add(this.parseCustomization(customization, customization.getType(), customization.getCategory(), customization.getIdentifier(), nestedObject.get(customization.getIdentifier()).getAsBoolean()));
                    nestedObject.remove(customization.getIdentifier());
                }
            }
        }

        for (String type : Arrays.asList("background", "shirt", "skin")) {
            if (!object.has(type)) {
                continue;
            }
            for (Map.Entry<String,JsonElement> entry : object.get(type).getAsJsonObject().entrySet()) {
                customizations.add(this.parseCustomization(null, type, null, entry.getKey(), entry.getValue().getAsBoolean()));
            }
        }
        if (object.has("hair")) {
            for (Map.Entry<String, JsonElement> categoryEntry : object.get("hair").getAsJsonObject().entrySet()) {
                for (Map.Entry<String, JsonElement> entry : categoryEntry.getValue().getAsJsonObject().entrySet()) {
                    customizations.add(this.parseCustomization(null, "hair", categoryEntry.getKey(), entry.getKey(), entry.getValue().getAsBoolean()));
                }
            }
        }

        TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(customizations)));

        purchases.customizations = customizations;

        return purchases;
    }

    private Customization parseCustomization(Customization existingCustomizaion, String type, String category, String key, boolean wasPurchased) {
        Customization customization = existingCustomizaion;
        if (customization == null) {
            customization = new Customization();
            customization.setIdentifier(key);
            customization.setType(type);
            if (category != null) {
                customization.setCategory(category);
            }
        }
        customization.setPurchased(wasPurchased);

        return customization;
    }
}
