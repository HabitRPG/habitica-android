package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.habitrpg.android.habitica.models.social.Challenge;

import android.text.TextUtils;

import java.lang.reflect.Type;

public class ChallengeSerializer implements JsonDeserializer<Challenge>, JsonSerializer<Challenge> {
    @Override
    public Challenge deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();

        Challenge challenge = new Challenge();

        challenge.id = jsonObject.get("id").getAsString();
        challenge.name = jsonObject.get("name").getAsString();
        challenge.shortName = jsonObject.get("shortName").getAsString();
        challenge.description = jsonObject.get("description").getAsString();
        challenge.memberCount = jsonObject.get("memberCount").getAsInt();

        JsonElement prizeElement = jsonObject.get("prize");
        if (!prizeElement.isJsonNull()) {
            challenge.prize = prizeElement.getAsInt();
        }

        challenge.official = jsonObject.get("official").getAsBoolean();

        JsonElement leaderElement = jsonObject.get("leader");

        if (leaderElement != null && !leaderElement.isJsonNull()) {
            JsonObject leaderObj = leaderElement.getAsJsonObject();

            if (leaderObj != null) {
                JsonObject profile = leaderObj.get("profile").getAsJsonObject();

                if (profile != null) {
                    challenge.leaderName = profile.get("name").getAsString();

                    JsonElement id = leaderObj.get("id");
                    if (id == null) {
                        id = leaderObj.get("_id");
                    }

                    if (id != null) {
                        challenge.leaderId = id.getAsString();
                    }
                }
            }
        }

        JsonElement groupElement = jsonObject.get("group");

        if (groupElement != null && !groupElement.isJsonNull()) {
            JsonObject groupObj = groupElement.getAsJsonObject();

            if (groupObj != null) {
                challenge.groupName = groupObj.get("name").getAsString();
                challenge.groupId = groupObj.get("_id").getAsString();
            }
        }

        JsonElement tasksOrderElement = jsonObject.get("tasksOrder");

        if (tasksOrderElement != null && !tasksOrderElement.isJsonNull()) {
            JsonObject tasksOrderObj = tasksOrderElement.getAsJsonObject();

            challenge.todoList = getTaskArrayAsString(context, tasksOrderObj, Challenge.TASK_ORDER_TODOS);
            challenge.dailyList = getTaskArrayAsString(context, tasksOrderObj, Challenge.TASK_ORDER_DAILYS);
            challenge.habitList = getTaskArrayAsString(context, tasksOrderObj, Challenge.TASK_ORDER_HABITS);
            challenge.rewardList = getTaskArrayAsString(context, tasksOrderObj, Challenge.TASK_ORDER_REWARDS);
        }

        return challenge;
    }

    private String getTaskArrayAsString(JsonDeserializationContext context, JsonObject tasksOrderObj, String taskType) {

        if (tasksOrderObj.has(taskType)) {
            JsonElement jsonElement = tasksOrderObj.get(taskType);

            String[] taskArray = context.deserialize(jsonElement, String[].class);

            return TextUtils.join(",", taskArray);
        }

        return "";
    }

    @Override
    public JsonElement serialize(Challenge src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("id", src.id);
        object.addProperty("name", src.name);
        object.addProperty("shortName", src.shortName);
        object.addProperty("description", src.description);
        object.addProperty("memberCount", src.memberCount);
        object.addProperty("prize", src.prize);
        object.addProperty("official", src.official);

        object.addProperty("group", src.groupId);
        object.add("tasksOrder", context.serialize(src.tasksOrder));


        return object;
    }
}
