package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.habitrpg.shared.habitica.models.inventory.Customization;
import com.habitrpg.android.habitica.models.user.Purchases;
import com.habitrpg.android.habitica.models.user.SubscriptionPlan;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;

public class PurchasedDeserializer implements JsonDeserializer<Purchases> {

    @Override
    public Purchases deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        RealmList<Customization> customizations = new RealmList<>();
        Purchases purchases = new Purchases();

        List<Customization> existingCustomizations;
        try {
            Realm realm = Realm.getDefaultInstance();
            existingCustomizations = realm.copyFromRealm(realm.where(Customization.class).findAll());
            realm.close();
        } catch (RuntimeException e) {
            //Tests don't have a database
            existingCustomizations = new ArrayList<>();
        }
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
                    customizations.add(this.parseCustomization(customization, customization.getType(), customization.getCategory(), customization.getIdentifier(), nestedObject.get(customization.getIdentifier()).getAsBoolean()));
                    nestedObject.remove(customization.getIdentifier());
                }
            }
        }

        for (String type : Arrays.asList("background", "shirt", "skin")) {
            if (!object.has(type)) {
                continue;
            }
            for (Map.Entry<String, JsonElement> entry : object.get(type).getAsJsonObject().entrySet()) {
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

        purchases.customizations = customizations;
        purchases.setPlan(context.deserialize(object.get("plan"), SubscriptionPlan.class));

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
