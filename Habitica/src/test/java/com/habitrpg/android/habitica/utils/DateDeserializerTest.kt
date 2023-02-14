package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import java.lang.reflect.Type
import java.util.Date

class DateDeserializerTest: WordSpec({
    val deserializer = DateDeserializer()
    val deserializationContext = object : JsonDeserializationContext {
        override fun <T> deserialize(json: JsonElement, typeOfT: Type): T? {
            return null
        }
    }
    val serializationContext = object : JsonSerializationContext {
        override fun serialize(src: Any): JsonElement? {
            return null
        }

        override fun serialize(src: Any, typeOfSrc: Type): JsonElement? {
            return null
        }
    }
    val referenceTimestamp: Long = 1443445200000

    "deserialize" should {
        "validate normal date" {
            val date = deserializer.deserialize(
                JsonPrimitive("2015-09-28T13:00:00.000Z"),
                Date::class.java,
                deserializationContext
            )
            date shouldBe Date(referenceTimestamp)
        }

        "validate timestamp" {
            val date = deserializer.deserialize(
                JsonPrimitive(referenceTimestamp),
                Date::class.java,
                deserializationContext
            )
            date shouldBe Date(referenceTimestamp)
        }

        "validate empty" {
            val date =
                deserializer.deserialize(JsonPrimitive(""), Date::class.java, deserializationContext)
            date shouldBe null
        }
    }

    "serialize" should {
        "serialize normal date" {
            val dateElement: JsonElement = deserializer.serialize(
                Date(
                    referenceTimestamp
                ),
                Date::class.java, serializationContext
            )
            dateElement.asString shouldBe "2015-09-28T13:00:00.000Z"
        }

        "serialize empty" {
            val dateElement: JsonElement =
                deserializer.serialize(null, Date::class.java, serializationContext)
            dateElement.asString shouldBe ""
        }
    }
})
