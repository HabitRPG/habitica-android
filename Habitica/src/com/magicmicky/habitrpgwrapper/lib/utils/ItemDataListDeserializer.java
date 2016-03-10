package com.magicmicky.habitrpgwrapper.lib.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.magicmicky.habitrpgwrapper.lib.models.Customization;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ItemData;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ItemDataListDeserializer implements JsonDeserializer<List<ItemData>> {
    @Override
    public List<ItemData> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        List<ItemData> vals = new ArrayList<>();

        List<ItemData> existingItems = new Select().from(ItemData.class).queryList();

        for (ItemData item : existingItems) {
            if(object.has(item.key)) {
                JsonElement itemObject = object.get(item.key);

                if (itemObject.isJsonObject()) {
                    ItemData parsedItem = context.deserialize(itemObject.getAsJsonObject(), ItemData.class);
                    item.text = parsedItem.text;
                    item.value = parsedItem.value;
                    item.type = parsedItem.type;
                    item.klass = parsedItem.klass;
                    item.index = parsedItem.index;
                    item.notes = parsedItem.notes;
                    item.con = parsedItem.con;
                    item.str = parsedItem.str;
                    item.per = parsedItem.per;
                    item._int = parsedItem._int;
                } else {
                    item.owned = itemObject.getAsBoolean();
                }
                vals.add(item);
                object.remove(item.key);
            }
        }

        for (Map.Entry<String,JsonElement> entry : json.getAsJsonObject().entrySet()) {
            ItemData item = context.deserialize(entry.getValue(), ItemData.class);
            vals.add(item);
        }
        TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(vals)));

        return vals;
    }
}
