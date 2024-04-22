package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.habitrpg.android.habitica.models.FAQArticle
import io.realm.RealmList
import java.lang.reflect.Type

/**
 * Created by viirus on 22/01/16.
 */
class FAQArticleListDeserilializer : JsonDeserializer<List<FAQArticle>> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): List<FAQArticle> {
        val articles = RealmList<FAQArticle>()
        for ((position, e) in json.asJsonObject.get("questions").asJsonArray.withIndex()) {
            val obj = e.asJsonObject
            val article = FAQArticle()
            article.position = position
            article.question = obj.get("question").asString
            if (obj.has("android")) {
                article.answer = obj.get("android").asString
            } else {
                article.answer = obj.get("web").asString
            }
            articles.add(article)
        }

        return articles
    }
}
