package com.magicmicky.habitrpgwrapper.lib.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.magicmicky.habitrpgwrapper.lib.models.ContentResult;
import com.magicmicky.habitrpgwrapper.lib.models.Skill;
import com.magicmicky.habitrpgwrapper.lib.models.SkillList;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by viirus on 25/11/15.
 */
public class SkillDeserializer
        implements JsonDeserializer<SkillList> {

    @Override
    public SkillList deserialize(JsonElement json, Type type,
                                 JsonDeserializationContext context) throws JsonParseException {

        JsonObject object = json.getAsJsonObject();
        List<Skill> skills = new ArrayList<Skill>();
        for (Map.Entry<String, JsonElement> classEntry : object.entrySet()) {
            String classname = classEntry.getKey();
            JsonObject classObject = classEntry.getValue().getAsJsonObject();
            if (classname.equals("special")) {
                continue;
            }
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
                skill.lvl = skillObject.get("lvl").getAsInt();
                skills.add(skill);
            }
        }

        TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(skills)));

        SkillList skillList = new SkillList();

        return skillList;
    }
}