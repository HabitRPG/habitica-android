package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.habitrpg.android.habitica.models.TutorialStep;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.realm.RealmList;

public class TutorialStepListDeserializer implements JsonDeserializer<List<TutorialStep>> {
    @Override
    public List<TutorialStep> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        RealmList<TutorialStep> vals = new RealmList<>();
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
