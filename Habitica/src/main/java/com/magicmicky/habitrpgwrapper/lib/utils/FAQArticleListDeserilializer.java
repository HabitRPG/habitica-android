package com.magicmicky.habitrpgwrapper.lib.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import com.magicmicky.habitrpgwrapper.lib.models.FAQArticle;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by viirus on 22/01/16.
 */
public class FAQArticleListDeserilializer implements JsonDeserializer<List<FAQArticle>> {


    @Override
    public List<FAQArticle> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<FAQArticle> vals = new ArrayList<>();
        int position = 0;
        for (JsonElement e : json.getAsJsonObject().get("questions").getAsJsonArray()) {
            JsonObject obj = e.getAsJsonObject();
            FAQArticle article = new FAQArticle();
            article.setPosition(position);
            article.setQuestion(obj.get("question").getAsString());
            if (obj.has("android")) {
                article.setAnswer(obj.get("android").getAsString());
            } else {
                article.setAnswer(obj.get("web").getAsString());
            }
            vals.add(article);
            position++;
        }

        return vals;
    }
}
