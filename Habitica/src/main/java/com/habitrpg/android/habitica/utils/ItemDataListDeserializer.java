package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.habitrpg.android.habitica.models.inventory.Equipment;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemDataListDeserializer implements JsonDeserializer<List<Equipment>> {
    @Override
    public List<Equipment> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<Equipment> vals = new ArrayList<>();
        if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();

            // TODO: fix this
            List<Equipment> existingItems = new ArrayList<>();

            for (Equipment item : existingItems) {
                if (object.has(item.key)) {
                    JsonElement itemObject = object.get(item.key);

                    if (itemObject.isJsonObject()) {
                        Equipment parsedItem = context.deserialize(itemObject.getAsJsonObject(), Equipment.class);
                        item.text = parsedItem.text;
                        item.value = parsedItem.value;
                        item.type = parsedItem.type;
                        item.klass = parsedItem.klass;
                        item.specialClass = parsedItem.specialClass;
                        item.index = parsedItem.index;
                        item.notes = parsedItem.notes;
                        item.con = parsedItem.con;
                        item.str = parsedItem.str;
                        item.per = parsedItem.per;
                        item._int = parsedItem._int;
                    }
                    vals.add(item);
                    object.remove(item.key);
                }
            }

            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                Equipment item;
                if (entry.getValue().isJsonObject()) {
                    item = context.deserialize(entry.getValue(), Equipment.class);
                } else {
                    item = new Equipment();
                    item.key = entry.getKey();
                }
                vals.add(item);
            }
        } else {
            for (JsonElement item : json.getAsJsonArray()) {
                vals.add((Equipment) context.deserialize(item.getAsJsonObject(), Equipment.class));
            }
        }

        return vals;
    }
}
