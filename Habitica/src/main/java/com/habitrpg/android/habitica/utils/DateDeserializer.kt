package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer

import java.lang.reflect.Type
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DateDeserializer : JsonDeserializer<Date>, JsonSerializer<Date> {

    private val dateFormat: DateFormat
    private val alternativeFormat: DateFormat
    private val nextDueFormat: DateFormat

    init {
        dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        alternativeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        alternativeFormat.timeZone = TimeZone.getTimeZone("UTC")
        nextDueFormat = SimpleDateFormat("E MMM dd yyyy HH:mm:ss zzzz", Locale.US)
        nextDueFormat.timeZone = TimeZone.getTimeZone("UTC")

    }

    @Synchronized
    @Suppress("ReturnCount")
    override fun deserialize(jsonElement: JsonElement, type: Type, jsonDeserializationContext: JsonDeserializationContext): Date? {
        var element = jsonElement
        if (element.isJsonArray) {
            if (element.asJsonArray.size() == 0) {
                return null
            }
            element = element.asJsonArray.get(0)
        }
        if (element.asString.isEmpty()) {
            return null
        }
        val jsonString = element.asString
        return try {
            dateFormat.parse(jsonString)
        } catch (e: ParseException) {
            try {
                alternativeFormat.parse(jsonString)
            } catch (e1: ParseException) {
                try {
                    nextDueFormat.parse(jsonString)
                } catch (e2: ParseException) {
                    try {
                        val timestamp = jsonElement.asLong
                        if (timestamp > 0) {
                            Date(timestamp)
                        } else {
                            null
                        }
                    } catch (e3: NumberFormatException) {
                        null
                    }

                }

            }

        }

    }

    override fun serialize(src: Date?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return if (src == null) {
            JsonPrimitive("")
        } else JsonPrimitive(this.dateFormat.format(src))
    }
}