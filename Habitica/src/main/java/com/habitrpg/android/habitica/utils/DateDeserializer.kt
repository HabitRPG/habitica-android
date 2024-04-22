package com.habitrpg.android.habitica.utils

import android.os.Build
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
    private var dateFormats = mutableListOf<DateFormat>()

    init {
        addFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        addFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        addFormat("E MMM dd yyyy HH:mm:ss zzzz")
        addFormat("yyyy-MM-dd'T'HH:mm:sszzz")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            addFormat("yyyy-MM-dd'T'HH:mmX")
        } else {
            addFormat("yyyy-MM-dd'T'HH:mm")
        }
        addFormat("yyyy-MM-dd")
    }

    private fun addFormat(s: String) {
        val dateFormat = SimpleDateFormat(s, Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        dateFormats.add(dateFormat)
    }

    @Synchronized
    @Suppress("ReturnCount")
    override fun deserialize(
        jsonElement: JsonElement,
        type: Type,
        jsonDeserializationContext: JsonDeserializationContext,
    ): Date? {
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
        var date: Date? = null
        var index = 0
        while (index < dateFormats.size && date == null) {
            try {
                date = dateFormats[index].parse(jsonString)
            } catch (_: ParseException) {
            }
            index += 1
        }
        if (date == null) {
            date =
                try {
                    Date(element.asLong)
                } catch (e3: NumberFormatException) {
                    null
                }
        }
        return date
    }

    override fun serialize(
        src: Date?,
        typeOfSrc: Type,
        context: JsonSerializationContext,
    ): JsonElement {
        return if (src == null) {
            JsonPrimitive("")
        } else {
            JsonPrimitive(this.dateFormats[0].format(src))
        }
    }
}
