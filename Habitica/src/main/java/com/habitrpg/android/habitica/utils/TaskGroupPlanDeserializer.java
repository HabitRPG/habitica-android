package com.habitrpg.android.habitica.utils;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.habitrpg.android.habitica.models.tasks.TaskGroupPlan;

import java.lang.reflect.Type;

public class TaskGroupPlanDeserializer implements JsonDeserializer<TaskGroupPlan> {
    @Override
    public TaskGroupPlan deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        TaskGroupPlan group = new TaskGroupPlan();
        JsonObject object = json.getAsJsonObject();
        JsonObject approvalObject = object.getAsJsonObject("approval");
        group.setApprovalRequested(approvalObject.getAsJsonPrimitive("requested").getAsBoolean());
        group.setApprovalApproved(approvalObject.getAsJsonPrimitive("approved").getAsBoolean());
        group.setApprovalRequired(approvalObject.getAsJsonPrimitive("required").getAsBoolean());

        return null;
    }
}
