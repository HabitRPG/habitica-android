package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.habitrpg.android.habitica.models.social.ChatMessage;

import java.lang.reflect.Type;
import java.util.Map;

import io.realm.RealmList;

public class ChatMessageListDeserializer implements JsonDeserializer<RealmList<ChatMessage>> {
    @Override
    public RealmList<ChatMessage> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        RealmList<ChatMessage> messages = new RealmList<>();

        if (json.isJsonArray()) {
            for (JsonElement element : json.getAsJsonArray()) {
                messages.add(context.deserialize(element, ChatMessage.class));
            }
        } else {
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                messages.add(context.deserialize(entry.getValue(), ChatMessage.class));
            }
        }

        return messages;
    }
}
