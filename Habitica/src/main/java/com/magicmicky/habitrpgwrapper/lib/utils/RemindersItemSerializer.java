package com.magicmicky.habitrpgwrapper.lib.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.RemindersItem;

import java.lang.reflect.Type;

/**
 * Created by keithholliday on 6/4/16.
 */
public class RemindersItemSerializer
        implements JsonSerializer<RemindersItem> {

    @Override
    public JsonElement serialize(RemindersItem src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("id", src.getId());
        object.addProperty("startDate", src.getStartDate().getTime());
        object.addProperty("time", src.getTime().getTime());
        return object;
    }
}
