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
        List<Skill> skills = new ArrayList<>();
        for (Map.Entry<String, JsonElement> classEntry : object.entrySet()) {
            String classname = classEntry.getKey();
            JsonObject classObject = classEntry.getValue().getAsJsonObject();

            for (Map.Entry<String, JsonElement> skillEntry : classObject.entrySet()) {
                JsonObject skillObject = skillEntry.getValue().getAsJsonObject();
                Skill skill = new Skill();
                skill.setKey(skillObject.get("key").getAsString());
                skill.setText(skillObject.get("text").getAsString());
                skill.setNotes(skillObject.get("notes").getAsString());
                skill.setKey(skillObject.get("key").getAsString());
                skill.setTarget(skillObject.get("target").getAsString());
                skill.setHabitClass(classname);
                skill.setMana(skillObject.get("mana").getAsInt());

                JsonElement lvlElement = skillObject.get("lvl");

                if (lvlElement != null) {
                    skill.setLvl(lvlElement.getAsInt());
                }

                skills.add(skill);
            }
        }

        return skills;
    }
}