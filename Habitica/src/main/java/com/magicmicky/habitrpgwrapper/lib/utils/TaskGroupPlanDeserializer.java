package com.magicmicky.habitrpgwrapper.lib.utils;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskGroupPlan;

import java.lang.reflect.Type;

public class TaskGroupPlanDeserializer implements JsonDeserializer<TaskGroupPlan> {
    @Override
    public TaskGroupPlan deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        TaskGroupPlan group = new TaskGroupPlan();
        JsonObject object = json.getAsJsonObject();
        JsonObject approvalObject = object.getAsJsonObject("approval");
        group.approvalRequested = approvalObject.getAsJsonPrimitive("requested").getAsBoolean();
        group.approvalApproved = approvalObject.getAsJsonPrimitive("approved").getAsBoolean();
        group.approvalRequired = approvalObject.getAsJsonPrimitive("required").getAsBoolean();

        return null;
    }
}
