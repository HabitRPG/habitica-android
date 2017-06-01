package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import com.habitrpg.android.habitica.models.Skill;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by viirus on 25/11/15.
 */
public class SkillDeserializer
        implements JsonDeserializer<List<Skill>> {

    @Override
    public List<Skill> deserialize(JsonElement json, Type type,
                                   JsonDeserializationContext context) throws JsonParseException {

        JsonObject object = json.getAsJsonObject();
        List<Skill> skills = new ArrayList<Skill>();
        for (Map.Entry<String, JsonElement> classEntry : object.entrySet()) {
            String classname = classEntry.getKey();
            JsonObject classObject = classEntry.getValue().getAsJsonObject();

            for (Map.Entry<String, JsonElement> skillEntry : classObject.entrySet()) {
                JsonObject skillObject = skillEntry.getValue().getAsJsonObject();
                Skill skill = new Skill();
                skill.key = skillObject.get("key").getAsString();
                skill.text = skillObject.get("text").getAsString();
                skill.notes = skillObject.get("notes").getAsString();
                skill.key = skillObject.get("key").getAsString();
                skill.target = skillObject.get("target").getAsString();
                skill.habitClass = classname;
                skill.mana = skillObject.get("mana").getAsInt();

                JsonElement lvlElement = skillObject.get("lvl");

                if (lvlElement != null) {
                    skill.lvl = lvlElement.getAsInt();
                }

                skills.add(skill);
            }
        }

        return skills;
    }
}