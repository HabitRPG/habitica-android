package com.habitrpg.android.habitica.utils

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException

class BooleanAsIntAdapter : TypeAdapter<Boolean>() {
    @Throws(IOException::class)
    override fun write(
        out: JsonWriter,
        value: Boolean?,
    ) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): Boolean? {
        return when (val peek = `in`.peek()) {
            JsonToken.BOOLEAN -> `in`.nextBoolean()
            JsonToken.NULL -> {
                `in`.nextNull()
                null
            }

            JsonToken.NUMBER -> `in`.nextInt() != 0
            JsonToken.STRING -> java.lang.Boolean.parseBoolean(`in`.nextString())
            else -> throw IllegalStateException("Expected BOOLEAN or NUMBER but was $peek")
        }
    }
}
