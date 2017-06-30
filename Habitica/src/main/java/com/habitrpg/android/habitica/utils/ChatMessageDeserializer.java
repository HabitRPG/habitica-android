package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.habitrpg.android.habitica.models.social.Backer;
import com.habitrpg.android.habitica.models.social.ChatMessage;
import com.habitrpg.android.habitica.models.social.ChatMessageLike;
import com.habitrpg.android.habitica.models.user.ContributorInfo;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import io.realm.RealmList;

public class ChatMessageDeserializer implements JsonDeserializer<ChatMessage> {
    @Override
    public ChatMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        ChatMessage message = new ChatMessage();
        JsonObject obj = json.getAsJsonObject();
        if (obj.has("id")) {
            message.id = obj.get("id").getAsString();
        }
        if (obj.has("text") && !obj.get("text").isJsonNull() && obj.get("text").isJsonPrimitive()) {
            message.text = obj.get("text").getAsString();
        }
        if (obj.has("timestamp")) {
            message.timestamp = obj.get("timestamp").getAsLong();
        }
        if (obj.has("likes")) {
            message.likes = new RealmList<>();
            for (Map.Entry<String, JsonElement> likeEntry : obj.getAsJsonObject("likes").entrySet()) {
                if (likeEntry.getValue().getAsBoolean()) {
                    message.likes.add(new ChatMessageLike(likeEntry.getKey()));
                }
            }
        }
        if (obj.has("flagCount")) {
            message.flagCount = obj.get("flagCount").getAsInt();
        }

        if (obj.has("uuid")) {
            message.uuid = obj.get("uuid").getAsString();
        }

        if (obj.has("contributor")) {
            if (!obj.get("contributor").isJsonNull()) {
                if (obj.get("contributor").isJsonObject()) {
                    message.contributor = context.deserialize(obj.get("contributor"), ContributorInfo.class);
                } else {
                    ContributorInfo contributor = new ContributorInfo();
                    contributor.setText(obj.get("contributor").getAsString());
                    message.contributor = contributor;
                }
                message.contributor.setUserId(message.id);
            }
        }

        if (obj.has("backer")) {
            message.backer = context.deserialize(obj.get("backer"), Backer.class);
        }

        if (obj.has("user")) {
            message.user = obj.get("user").getAsString();
        }

        if (obj.has("sent")) {
            message.sent = obj.get("sent").getAsString();
        }

        return message;
    }
}
