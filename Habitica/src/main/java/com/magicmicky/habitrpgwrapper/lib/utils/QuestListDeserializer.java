package com.magicmicky.habitrpgwrapper.lib.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import com.magicmicky.habitrpgwrapper.lib.models.inventory.QuestContent;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuestListDeserializer implements JsonDeserializer<List<QuestContent>> {
    @Override
    public List<QuestContent> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<QuestContent> vals = new ArrayList<>();
        if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();

            List<QuestContent> existingItems = new Select().from(QuestContent.class).queryList();

            for (QuestContent item : existingItems) {
                if(object.has(item.getKey())) {
                    JsonElement itemObject = object.get(item.getKey());

                    if (itemObject.isJsonObject()) {
                        QuestContent parsedItem = context.deserialize(itemObject.getAsJsonObject(), QuestContent.class);
                        item.setText(parsedItem.getText());
                        item.setNotes(parsedItem.getNotes());
                        item.setValue(parsedItem.getValue());
                        item.setPrevious(parsedItem.getPrevious());
                        item.setCanBuy(parsedItem.isCanBuy());
                        item.setBoss(parsedItem.getBoss());
                        item.setCategory(parsedItem.getCategory());
                        item.setCollect(parsedItem.getCollect());
                        item.setLvl(parsedItem.getLvl());
                    } else {
                        item.setOwned(itemObject.getAsInt());
                    }
                    vals.add(item);
                    object.remove(item.getKey());
                }
            }

            for (Map.Entry<String,JsonElement> entry : json.getAsJsonObject().entrySet()) {
                QuestContent item;
                if (entry.getValue().isJsonObject()) {
                    item = context.deserialize(entry.getValue(), QuestContent.class);
                } else {
                    item = new QuestContent();
                    item.setKey(entry.getKey());
                    if (entry.getValue().isJsonNull()) {
                        item.setOwned(0);
                    } else {
                        item.setOwned(entry.getValue().getAsInt());
                    }
                }
                vals.add(item);
            }
            TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(vals)));
        } else {
            for (JsonElement item : json.getAsJsonArray()) {
                vals.add((QuestContent) context.deserialize(item.getAsJsonObject(), QuestContent.class));
            }
        }

        return vals;
    }
}
