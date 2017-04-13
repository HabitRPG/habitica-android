package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import com.habitrpg.android.habitica.models.inventory.Food;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FoodListDeserializer implements JsonDeserializer<List<Food>> {
    @Override
    public List<Food> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<Food> vals = new ArrayList<>();
        if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();

            List<Food> existingItems = new Select().from(Food.class).queryList();

            for (Food item : existingItems) {
                if (object.has(item.getKey())) {
                    JsonElement itemObject = object.get(item.getKey());

                    if (itemObject.isJsonObject()) {
                        Food parsedItem = context.deserialize(itemObject.getAsJsonObject(), Food.class);
                        item.setText(parsedItem.getText());
                        item.setNotes(parsedItem.getNotes());
                        item.setValue(parsedItem.getValue());
                        item.setArticle(parsedItem.getArticle());
                        item.setCanDrop(parsedItem.getCanDrop());
                        item.setTarget(parsedItem.getTarget());
                    } else {
                        item.setOwned(itemObject.getAsInt());
                    }
                    vals.add(item);
                    object.remove(item.getKey());
                }
            }

            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                Food item;
                if (entry.getValue().isJsonObject()) {
                    item = context.deserialize(entry.getValue(), Food.class);
                } else {
                    item = new Food();
                    item.setKey(entry.getKey());
                    if (entry.getValue().isJsonNull()) {
                        item.setOwned(0);
                    } else {
                        item.setOwned(entry.getValue().getAsInt());
                    }
                }
                vals.add(item);
            }
        } else {
            for (JsonElement item : json.getAsJsonArray()) {
                vals.add((Food) context.deserialize(item.getAsJsonObject(), Food.class));
            }
        }

        return vals;
    }
}
