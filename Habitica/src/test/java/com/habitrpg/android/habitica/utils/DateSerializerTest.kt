package com.habitrpg.android.habitica.utils

import com.habitrpg.shared.habitica.serialization.DateSerializer
import io.kotest.matchers.shouldBe
import kotlinx.serialization.builtins.nullable
import java.util.Date

class DateSerializerTest :
    SerializerSpec({
        val referenceTimestamp = 1443445200000
        val serializer = DateSerializer.nullable

        "deserialize" should {
            "read normal dates" {
                parse(serializer, "\"2015-09-28T13:00:00.000Z\"") shouldBe Date(referenceTimestamp)
            }

            "read timestamps" {
                parse(serializer, "$referenceTimestamp") shouldBe Date(referenceTimestamp)
            }

            "read empty strings as null" {
                parse(serializer, "\"\"") shouldBe null
            }

            "use the first entry of an array" {
                parse(serializer, "[\"2015-09-28T13:00:00.000Z\"]") shouldBe Date(referenceTimestamp)
            }

            "return null for an empty array" {
                parse(serializer, "[]") shouldBe null
            }

            "parse dates without milliseconds or time" {
                parse(serializer, "\"2015-09-28T13:00:00Z\"") shouldBe Date(referenceTimestamp)
                parse(serializer, "\"2015-09-28\"") shouldBe Date(1443398400000)
            }

            "return null for unparseable strings" {
                parse(serializer, "\"not a date\"") shouldBe null
            }
        }

        "serialize" should {
            "write dates in the API format" {
                json.encodeToString(serializer, Date(referenceTimestamp)) shouldBe "\"2015-09-28T13:00:00.000Z\""
            }

            "write null as null" {
                json.encodeToString(serializer, null) shouldBe "null"
            }
        }
    })
