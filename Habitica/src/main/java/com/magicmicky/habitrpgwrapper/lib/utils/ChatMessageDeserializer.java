package com.magicmicky.habitrpgwrapper.lib.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import com.magicmicky.habitrpgwrapper.lib.models.Backer;
import com.magicmicky.habitrpgwrapper.lib.models.ChatMessage;
import com.magicmicky.habitrpgwrapper.lib.models.Contributor;

import java.lang.reflect.Type;
import java.util.HashMap;

public class ChatMessageDeserializer implements JsonDeserializer<ChatMessage> {
    @Override
    public ChatMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        ChatMessage message = new ChatMessage();
        JsonObject obj = json.getAsJsonObject();
        if (obj.has("id")) {
            message.id = obj.get("id").getAsString();
        }
        if (obj.has("text") && !obj.get("text").isJsonNull()) {
            message.text = obj.get("text").getAsString();
        }
        if (obj.has("timestamp")) {
            message.timestamp = obj.get("timestamp").getAsLong();
        }
        if (obj.has("likes")) {
            message.likes = context.deserialize(obj.get("likes"), new TypeToken<HashMap<String, Boolean>>() {}.getType());
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
                    message.contributor = context.deserialize(obj.get("contributor"), Contributor.class);
                } else {
                    Contributor contributor = new Contributor();
                    contributor.text = obj.get("contributor").getAsString();
                    message.contributor = contributor;
                }
            }
        }
        if (obj.has("backer")) {
            message.backer = context.deserialize(obj.get("backer"), Backer.class);
        }
        if (obj.has("user")) {
            message.user = obj.get("user").getAsString();
        }

        return message;
    }
}
