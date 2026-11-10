package com.habitrpg.android.habitica.api

import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.utils.AchievementListSerializer
import com.habitrpg.shared.habitica.models.HabitResponse
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
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

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
        val traceName = traceNames[dataType(type).upperBound]
        return Converter<ResponseBody, Any?> { body ->
            // Read first, so the trace only measures parsing and not the download
            val text = body.use { it.string() }
            json.decodeFromString(serializer, text)
        }
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
    private fun serializerFor(type: Type): KSerializer<Any?> {
        if (type is ParameterizedType && type.rawType == HabitResponse::class.java) {
            return HabitResponse.serializer(serializerFor(type.actualTypeArguments[0])) as KSerializer<Any?>
        }
        // The member achievements endpoint sends an object of categories instead of a list
        if (type is ParameterizedType &&
            type.rawType == List::class.java &&
            type.actualTypeArguments[0].upperBound == Achievement::class.java
        ) {
            return AchievementListSerializer as KSerializer<Any?>
        }
        return json.serializersModule.serializer(type) as KSerializer<Any?>
    }

    // Kotlin declares `List<Achievement>` as `List<? extends Achievement>` in some positions
    private val Type.upperBound: Type
        get() = (this as? WildcardType)?.upperBounds?.first() ?: this

    private fun dataType(type: Type): Type =
        if (type is ParameterizedType && type.rawType == HabitResponse::class.java) type.actualTypeArguments[0] else type

    companion object {
        // Large responses whose parsing time is tracked
        private val traceNames =
            mapOf(
                User::class.java to "UserDeserialize",
                ContentResult::class.java to "ContentDeserialize",
                TaskList::class.java to "TaskListDeserialize",
            )
    }
}
