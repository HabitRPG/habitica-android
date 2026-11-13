package com.habitrpg.android.habitica.utils

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import io.kotest.matchers.shouldBe
import java.util.Date

class DateDeserializerTest :
    SerializerSpec({
        val deserializer = DateDeserializer()
        val referenceTimestamp = 1443445200000

        "deserialize" should {
            "validate normal date" {
                val date =
                    deserializer.deserialize(
                        JsonPrimitive("2015-09-28T13:00:00.000Z"),
                        Date::class.java,
                        deserializationContext,
                    )
                date shouldBe Date(referenceTimestamp)
            }

            "validate timestamp" {
                val date =
                    deserializer.deserialize(
                        JsonPrimitive(referenceTimestamp),
                        Date::class.java,
                        deserializationContext,
                    )
                date shouldBe Date(referenceTimestamp)
            }

            "validate empty" {
                val date =
                    deserializer.deserialize(JsonPrimitive(""), Date::class.java, deserializationContext)
                date shouldBe null
            }

            "use the first entry of an array" {
                val array = JsonArray().apply { add("2015-09-28T13:00:00.000Z") }
                deserializer.deserialize(array, Date::class.java, deserializationContext) shouldBe Date(referenceTimestamp)
            }

            "return null for an empty array" {
                deserializer.deserialize(JsonArray(), Date::class.java, deserializationContext) shouldBe null
            }

            "parse dates without milliseconds or time" {
                deserializer.deserialize(JsonPrimitive("2015-09-28T13:00:00Z"), Date::class.java, deserializationContext) shouldBe
                    Date(referenceTimestamp)
                deserializer.deserialize(JsonPrimitive("2015-09-28"), Date::class.java, deserializationContext) shouldBe
                    Date(1443398400000)
            }

            "return null for unparseable strings" {
                deserializer.deserialize(JsonPrimitive("not a date"), Date::class.java, deserializationContext) shouldBe null
            }
        }

        "serialize" should {
            "serialize normal date" {
                val dateElement: JsonElement =
                    deserializer.serialize(
                        Date(
                            referenceTimestamp,
                        ),
                        Date::class.java,
                        serializationContext,
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
