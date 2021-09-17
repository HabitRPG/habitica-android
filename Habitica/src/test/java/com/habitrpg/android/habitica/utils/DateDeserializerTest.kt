package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.habitrpg.android.habitica.BaseAnnotationTestCase
import io.kotest.matchers.shouldBe
import java.lang.reflect.Type
import java.util.Date

class DateDeserializerTest: BaseAnnotationTestCase() {
    var deserializer = DateDeserializer()
    lateinit var deserializationContext: JsonDeserializationContext
    lateinit var serializationContext: JsonSerializationContext
    var referenceTimestamp: Long = 1443445200000

    @Before
    fun setup() {
        deserializationContext = object : JsonDeserializationContext {
            override fun <T> deserialize(json: JsonElement, typeOfT: Type): T? {
                return null
            }
        }
        serializationContext = object : JsonSerializationContext {
            override fun serialize(src: Any): JsonElement? {
                return null
            }

            override fun serialize(src: Any, typeOfSrc: Type): JsonElement? {
                return null
            }
        }
    }

    @Test
    fun validateNormalDateDeserialize() {
        val date = deserializer.deserialize(
            JsonPrimitive("2015-09-28T13:00:00.000Z"),
            Date::class.java,
            deserializationContext
        )
        date shouldBe Date(referenceTimestamp)
    }

    @Test
    fun validateTimestampDeserialize() {
        val date = deserializer.deserialize(
            JsonPrimitive(referenceTimestamp),
            Date::class.java,
            deserializationContext
        )
        date shouldBe Date(referenceTimestamp)
    }

    @Test
    fun validateEmptyDeserialize() {
        val date =
            deserializer.deserialize(JsonPrimitive(""), Date::class.java, deserializationContext)
        date shouldBe null
    }

    @Test
    fun validateNormalDateSerialize() {
        val dateElement: JsonElement = deserializer!!.serialize(
            Date(
                referenceTimestamp!!
            ), Date::class.java, serializationContext
        )
        dateElement.asString shouldBe "2015-09-28T13:00:00.000Z"
    }

    @Test
    fun validateEmptySerialize() {
        val dateElement: JsonElement =
            deserializer!!.serialize(null, Date::class.java, serializationContext)
        dateElement.asString shouldBe ""
    }
}