package com.habitrpg.wearos.habitica.data

import com.habitrpg.shared.habitica.serialization.habiticaJson
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/** Converts API bodies with kotlinx.serialization, using [habiticaJson]. */
class HabiticaConverterFactory(
    private val json: Json = habiticaJson,
) : Converter.Factory() {
    private val contentType = "application/json".toMediaType()

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<ResponseBody, *> {
        val serializer = serializerFor(type)
        return Converter<ResponseBody, Any?> { body -> body.use { json.decodeFromString(serializer, it.string()) } }
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<*, RequestBody> {
        val serializer = serializerFor(type)
        return Converter<Any?, RequestBody> { json.encodeToString(serializer, it).toRequestBody(contentType) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun serializerFor(type: Type): KSerializer<Any?> = json.serializersModule.serializer(type) as KSerializer<Any?>
}
