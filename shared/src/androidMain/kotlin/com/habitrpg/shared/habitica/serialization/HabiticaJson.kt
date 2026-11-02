package com.habitrpg.shared.habitica.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** The JSON configuration for everything the apps exchange with the Habitica API. */
val habiticaJson =
    Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
        serializersModule =
            SerializersModule {
                contextual(Date::class, DateSerializer)
                @Suppress("UNCHECKED_CAST")
                contextual(Any::class, AnySerializer as KSerializer<Any>)
                @Suppress("UNCHECKED_CAST")
                contextual(Void::class, VoidSerializer as KSerializer<Void>)
            }
    }

@Suppress("UNCHECKED_CAST")
private fun <T> nullValue(): T = null as T

/**
 * Reads every date format the API has used. Empty or unreadable dates become null, even for fields
 * declared non-null, which is what the previous Gson setup did.
 */
object DateSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    private val formats =
        listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "E MMM dd yyyy HH:mm:ss zzzz",
            "yyyy-MM-dd'T'HH:mm:sszzz",
            "yyyy-MM-dd'T'HH:mmX",
            "yyyy-MM-dd",
        ).map { SimpleDateFormat(it, Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") } }

    override fun deserialize(decoder: Decoder): Date {
        var element = (decoder as JsonDecoder).decodeJsonElement()
        if (element is JsonArray) element = element.firstOrNull() ?: JsonNull
        val text = (element as? JsonPrimitive)?.takeUnless { it is JsonNull }?.content
        return parse(text) ?: nullValue()
    }

    fun parse(text: String?): Date? {
        if (text.isNullOrEmpty()) return null
        synchronized(formats) {
            for (format in formats) {
                try {
                    return format.parse(text)
                } catch (_: ParseException) {
                }
            }
            return text.toLongOrNull()?.let { Date(it) }
        }
    }

    fun format(date: Date): String = synchronized(formats) { formats[0].format(date) }

    override fun serialize(
        encoder: Encoder,
        value: Date,
    ) = encoder.encodeString(format(value))
}

/** Untyped values, as in `Map<String, Any?>` request bodies. Reads JSON as plain maps, lists, strings, doubles and booleans. */
object AnySerializer : KSerializer<Any?> {
    override val descriptor: SerialDescriptor = JsonElement.serializer().descriptor

    override fun deserialize(decoder: Decoder): Any? = (decoder as JsonDecoder).decodeJsonElement().toValue()

    override fun serialize(
        encoder: Encoder,
        value: Any?,
    ) {
        val output = encoder as JsonEncoder
        output.encodeJsonElement(output.json.toElement(value))
    }

    private fun JsonElement.toValue(): Any? =
        when (this) {
            is JsonNull -> null
            is JsonObject -> mapValues { it.value.toValue() }
            is JsonArray -> map { it.toValue() }
            is JsonPrimitive -> if (isString) content else booleanOrNull ?: doubleOrNull
        }

    private fun Json.toElement(value: Any?): JsonElement =
        when (value) {
            null -> JsonNull
            is JsonElement -> value
            is String -> JsonPrimitive(value)
            is Number -> JsonPrimitive(value)
            is Boolean -> JsonPrimitive(value)
            is Enum<*> -> JsonPrimitive(value.name)
            is Date -> JsonPrimitive(DateSerializer.format(value))
            is Map<*, *> -> JsonObject(value.entries.associate { it.key.toString() to toElement(it.value) })
            is Iterable<*> -> JsonArray(value.map { toElement(it) })
            is Array<*> -> JsonArray(value.map { toElement(it) })
            else -> encodeToJsonElement(serializersModule.serializer(value.javaClass), value)
        }
}

/** Bodies the app ignores, declared as `Void` in the API. */
object VoidSerializer : KSerializer<Any?> {
    override val descriptor: SerialDescriptor = JsonElement.serializer().descriptor

    override fun deserialize(decoder: Decoder): Any? {
        (decoder as JsonDecoder).decodeJsonElement()
        return null
    }

    override fun serialize(
        encoder: Encoder,
        value: Any?,
    ) = (encoder as JsonEncoder).encodeJsonElement(JsonNull)
}
