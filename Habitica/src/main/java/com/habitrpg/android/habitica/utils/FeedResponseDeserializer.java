package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.habitrpg.android.habitica.models.responses.FeedResponse;

import java.lang.reflect.Type;

public class FeedResponseDeserializer implements JsonDeserializer<FeedResponse> {
    @Override
    public FeedResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        FeedResponse response = new FeedResponse();
        response.value = json.getAsInt();
        return response;
    }
}
