package com.magicmicky.habitrpgwrapper.lib.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import com.magicmicky.habitrpgwrapper.lib.models.TutorialStep;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TutorialStepListDeserializer implements JsonDeserializer<List<TutorialStep>> {
    @Override
    public List<TutorialStep> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<TutorialStep> vals = new ArrayList<>();
        for (String group : Arrays.asList("common", "android")) {
            if (json.getAsJsonObject().has(group)) {
                for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().get(group).getAsJsonObject().entrySet()) {

                    vals.add(parseStep(group, entry));
                }
            }
        }

        return vals;
    }

    private TutorialStep parseStep(String group, Map.Entry<String, JsonElement> entry) {
        TutorialStep article = new TutorialStep();
        article.setTutorialGroup(group);
        article.setIdentifier(entry.getKey());
        article.setWasCompleted(entry.getValue().getAsBoolean());
        return article;
    }
}
