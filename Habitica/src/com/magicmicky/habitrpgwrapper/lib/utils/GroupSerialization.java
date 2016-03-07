package com.magicmicky.habitrpgwrapper.lib.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.magicmicky.habitrpgwrapper.lib.models.ChatMessage;
import com.magicmicky.habitrpgwrapper.lib.models.Group;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Quest;

import java.lang.reflect.Type;
import java.util.List;

public class GroupSerialization implements JsonDeserializer<Group>, JsonSerializer<Group> {
    @Override
    public Group deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Group group = new Group();
        JsonObject obj = json.getAsJsonObject();
        group.id = obj.get("_id").getAsString();
        group.name = obj.get("name").getAsString();
        if (obj.has("description")) {
            group.description = obj.get("description").getAsString();
        }
        if (obj.has("privacy")) {
            group.privacy = obj.get("privacy").getAsString();
        }
        if (obj.has("memberCount")) {
            group.memberCount = obj.get("memberCount").getAsInt();
        }
        if (obj.has("balance")) {
            group.balance = obj.get("balance").getAsDouble();
        }
        if (obj.has("logo")) {
            group.logo = obj.get("logo").getAsString();
        }
        if (obj.has("type")) {
            group.type = obj.get("type").getAsString();
        }
        if (obj.has("chat")) {
            group.chat = context.deserialize(obj.get("chat"), new TypeToken<List<ChatMessage>>() {
            }.getType());
        }
        if (obj.has("members")) {
            group.members = context.deserialize(obj.get("members"), new TypeToken<List<HabitRPGUser>>(){}.getType());
        }
        if (obj.has("leader")) {
            if (obj.get("leader").isJsonPrimitive()) {
                group.leaderID = obj.get("leader").getAsString();
            } else {
                group.leaderID = obj.get("leader").getAsJsonObject().get("_id").getAsString();
            }
        }
        if (obj.has("quest")) {
            group.quest = context.deserialize(obj.get("quest"), new TypeToken<Quest>() {
            }.getType());
        }

        return group;
    }

    @Override
    public JsonElement serialize(Group src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", src.name);
        obj.addProperty("description", src.description);
        obj.addProperty("logo", src.logo);
        obj.addProperty("type", src.type);
        obj.addProperty("type", src.type);
        obj.addProperty("leader", src.leaderID);
        return obj;
    }
}
