package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.habitrpg.android.habitica.models.inventory.QuestCollect;

import java.lang.reflect.Type;
import java.util.Map;

import io.realm.RealmList;

public class QuestCollectDeserializer implements JsonDeserializer<RealmList<QuestCollect>> {
    @Override
    public RealmList<QuestCollect> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        RealmList<QuestCollect> items = new RealmList<>();

        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
            QuestCollect questCollect = new QuestCollect();
            questCollect.key = entry.getKey();
            JsonObject jsonObject = entry.getValue().getAsJsonObject();
            questCollect.count = jsonObject.get("count").getAsInt();
            questCollect.text = jsonObject.get("text").getAsString();
            items.add(questCollect);
        }

        return items;
    }
}
