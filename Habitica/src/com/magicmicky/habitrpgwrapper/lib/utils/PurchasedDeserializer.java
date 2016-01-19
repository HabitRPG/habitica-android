package com.magicmicky.habitrpgwrapper.lib.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.magicmicky.habitrpgwrapper.lib.models.Customization;
import com.magicmicky.habitrpgwrapper.lib.models.Purchases;
import com.magicmicky.habitrpgwrapper.lib.models.Skill;
import com.magicmicky.habitrpgwrapper.lib.models.SkillList;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ChecklistItem;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;

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

        for (String type : Arrays.asList("background", "shirt", "skin")) {
            if (!object.has(type)) {
                continue;
            }
            for (Map.Entry<String,JsonElement> entry : object.get(type).getAsJsonObject().entrySet()) {
                Customization customization = new Customization();
                customization.setType(type);
                customization.setIdentifier(entry.getKey());
                customization.setPurchased(entry.getValue().getAsBoolean());
                customizations.add(customization);
            }
        }
        if (object.has("hair")) {
            for (Map.Entry<String, JsonElement> categoryEntry : object.get("hair").getAsJsonObject().entrySet()) {
                for (Map.Entry<String, JsonElement> entry : categoryEntry.getValue().getAsJsonObject().entrySet()) {
                    Customization customization = new Customization();
                    customization.setType("hair");
                    customization.setCategory(categoryEntry.getKey());
                    customization.setIdentifier(entry.getKey());
                    customization.setPurchased(entry.getValue().getAsBoolean());
                    customizations.add(customization);
                }
            }
        }

        TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(customizations)));

        purchases.customizations = customizations;

        return purchases;
    }
}
