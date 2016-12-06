package com.magicmicky.habitrpgwrapper.lib.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.magicmicky.habitrpgwrapper.lib.models.Challenge;

import java.lang.reflect.Type;

public class ChallengeDeserializer implements JsonDeserializer<Challenge> {
    @Override
    public Challenge deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        Challenge challenge = new Challenge();
        challenge.id = jsonObject.get("id").getAsString();
        challenge.name = jsonObject.get("name").getAsString();
        challenge.shortName = jsonObject.get("shortName").getAsString();
        challenge.description = jsonObject.get("description").getAsString();
        challenge.official = jsonObject.get("official").getAsBoolean();
        challenge.memberCount = jsonObject.get("memberCount").getAsInt();

        JsonObject leaderObj = jsonObject.get("leader").getAsJsonObject();

        if (leaderObj != null) {
            JsonObject profile = leaderObj.get("profile").getAsJsonObject();

            if(profile != null){
                challenge.leaderName = profile.get("name").getAsString();
            }
        }

        return challenge
                ;
    }
}
