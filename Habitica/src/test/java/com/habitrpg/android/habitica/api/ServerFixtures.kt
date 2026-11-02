package com.habitrpg.android.habitica.api

import com.habitrpg.shared.habitica.models.HabitResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

/** Runs real server responses from test/resources/fixtures through the converter the app uses. */
object ServerFixtures {
    val converterFactory: Converter.Factory = HabiticaConverterFactory()

    private val retrofit =
        Retrofit
            .Builder()
            .baseUrl("https://habitica.com/api/v4/")
            .addConverterFactory(converterFactory)
            .build()

    fun read(name: String): String =
        requireNotNull(javaClass.classLoader?.getResource("fixtures/$name.json")) { "missing fixture $name" }.readText()

    fun <T> parseBody(
        json: String,
        type: Type,
    ): T {
        val converter = retrofit.responseBodyConverter<T>(type, emptyArray())
        return converter.convert(json.toResponseBody("application/json".toMediaType()))!!
    }

    @OptIn(ExperimentalStdlibApi::class)
    inline fun <reified T> parse(json: String): T = parseBody(json, typeOf<T>().javaType)

    @OptIn(ExperimentalStdlibApi::class)
    inline fun <reified T> fixture(name: String): HabitResponse<T> = parse(read(name))

    @OptIn(ExperimentalStdlibApi::class)
    inline fun <reified T> serialize(value: T): String = serializeBody(value, typeOf<T>().javaType)

    fun <T> serializeBody(
        value: T,
        type: Type,
    ): String {
        val converter = retrofit.requestBodyConverter<T>(type, emptyArray(), emptyArray())
        return Buffer().also { converter.convert(value)!!.writeTo(it) }.readUtf8()
    }
}
