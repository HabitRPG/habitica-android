package com.magicmicky.habitrpgwrapper.lib.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.ChecklistItem;

import java.lang.reflect.Type;

/**
 * Created by viirus on 25/11/15.
 */
public class ChecklistItemSerializer
        implements JsonSerializer<ChecklistItem> {

    @Override
    public JsonElement serialize(ChecklistItem src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("text", src.getText());
        object.addProperty("id", src.getId());
        object.addProperty("completed", src.getCompleted());
        return object;
    }
}