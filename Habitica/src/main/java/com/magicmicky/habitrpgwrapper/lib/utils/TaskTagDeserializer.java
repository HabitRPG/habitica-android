package com.magicmicky.habitrpgwrapper.lib.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import com.magicmicky.habitrpgwrapper.lib.models.Tag;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskTag;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TaskTagDeserializer implements JsonDeserializer<List<TaskTag>> {
    @Override
    public List<TaskTag> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<TaskTag> taskTags = new ArrayList<>();
        List<Tag> allTags;
        try {
            allTags = new Select()
                    .from(Tag.class)
                    .queryList();
        } catch (RuntimeException e) {
            //Tests don't have a database
            allTags = new ArrayList<>();
        }

        if (json.isJsonArray()) {
            for (JsonElement tagElement : json.getAsJsonArray()) {
                String tagId = tagElement.getAsString();
                TaskTag taskTag = new TaskTag();
                for (Tag tag : allTags) {
                    if (tag.getId().equals(tagId)) {
                        taskTag.setTag(tag);

                        if (!alreadyContainsTag(taskTags, tagId)) {
                            taskTags.add(taskTag);
                        }

                        break;
                    }
                }
            }
        }

        return taskTags;
    }

    private boolean alreadyContainsTag(List<TaskTag> list, String idToCheck) {
        for (TaskTag t : list) {
            if (t.getTag().getId().equals(idToCheck)) {
                return true;
            }
        }

        return false;
    }
}
