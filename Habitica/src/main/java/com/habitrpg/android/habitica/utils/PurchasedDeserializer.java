package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.habitrpg.android.habitica.models.user.OwnedCustomization;
import com.habitrpg.android.habitica.models.user.Purchases;
import com.habitrpg.android.habitica.models.user.SubscriptionPlan;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

import io.realm.RealmList;

public class PurchasedDeserializer implements JsonDeserializer<Purchases> {

    @Override
    public Purchases deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        RealmList<OwnedCustomization> customizations = new RealmList<>();
        Purchases purchases = new Purchases();

        for (String type : Arrays.asList("background", "shirt", "skin")) {
            if (!object.has(type)) {
                continue;
            }
            for (Map.Entry<String, JsonElement> entry : object.get(type).getAsJsonObject().entrySet()) {
                customizations.add(this.parseCustomization(type, null, entry.getKey(), entry.getValue().getAsBoolean()));
            }
        }
        if (object.has("hair")) {
            for (Map.Entry<String, JsonElement> categoryEntry : object.get("hair").getAsJsonObject().entrySet()) {
                for (Map.Entry<String, JsonElement> entry : categoryEntry.getValue().getAsJsonObject().entrySet()) {
                    customizations.add(this.parseCustomization("hair", categoryEntry.getKey(), entry.getKey(), entry.getValue().getAsBoolean()));
                }
            }
        }

        purchases.customizations = customizations;
        purchases.setPlan(context.deserialize(object.get("plan"), SubscriptionPlan.class));

        return purchases;
    }

    private OwnedCustomization parseCustomization(String type, String category, String key, boolean wasPurchased) {
        OwnedCustomization customization = new OwnedCustomization();
        customization.setKey(key);
        customization.setType(type);
        if (category != null) {
            customization.setCategory(category);
        }
        customization.setPurchased(wasPurchased);

        return customization;
    }
}
