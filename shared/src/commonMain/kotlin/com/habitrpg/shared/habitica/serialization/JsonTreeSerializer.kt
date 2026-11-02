package com.habitrpg.shared.habitica.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
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
import kotlinx.serialization.serializer

/**
 * Serializer for types whose JSON shape differs from the model, so they are read from the parsed JSON tree.
 * Only usable with [Json], which is the only format the app uses.
 */
abstract class JsonTreeSerializer<T>(
    serialName: String,
) : KSerializer<T> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(serialName)

    abstract fun Json.read(element: JsonElement): T

    open fun Json.write(value: T): JsonElement = throw SerializationException("${descriptor.serialName} can only be deserialized")

    override fun deserialize(decoder: Decoder): T {
        val input = decoder as JsonDecoder
        return input.json.read(input.decodeJsonElement())
    }

    override fun serialize(
        encoder: Encoder,
        value: T,
    ) {
        val output = encoder as JsonEncoder
        output.encodeJsonElement(output.json.write(value))
    }
}

/** Decodes [element] with the serializer registered for [T], or returns null for a missing or null element. */
inline fun <reified T> Json.decode(element: JsonElement?): T? =
    if (element == null || element is JsonNull) null else decodeFromJsonElement(serializersModule.serializer<T>(), element)

inline fun <reified T> Json.encode(value: T): JsonElement = encodeToJsonElement(serializersModule.serializer<T>(), value)

fun JsonObject.primitive(key: String): JsonPrimitive? = (this[key] as? JsonPrimitive)?.takeUnless { it is JsonNull }

fun JsonObject.obj(key: String): JsonObject? = this[key] as? JsonObject

fun JsonObject.array(key: String): JsonArray? = this[key] as? JsonArray

fun JsonObject.string(key: String): String? = primitive(key)?.content

fun JsonObject.bool(key: String): Boolean? = primitive(key)?.booleanOrNull

fun JsonObject.int(key: String): Int? = primitive(key)?.doubleOrNull?.toInt()

fun JsonObject.double(key: String): Double? = primitive(key)?.doubleOrNull
