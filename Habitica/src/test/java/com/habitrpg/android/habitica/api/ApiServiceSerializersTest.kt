package com.habitrpg.android.habitica.api

import io.kotest.core.spec.style.WordSpec
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

/** Every body type the API declares needs a serializer, otherwise the call fails at runtime. */
class ApiServiceSerializersTest :
    WordSpec({
        val retrofit =
            Retrofit
                .Builder()
                .baseUrl("https://habitica.com/api/v4/")
                .addConverterFactory(ServerFixtures.converterFactory)
                .build()

        // suspend functions declare their result as the last parameter: Continuation<Response<T>>
        fun responseBodyType(method: java.lang.reflect.Method): Type {
            val continuation = method.genericParameterTypes.last() as ParameterizedType
            val result = continuation.actualTypeArguments[0].let { (it as? WildcardType)?.lowerBounds?.first() ?: it }
            return if (result is ParameterizedType && result.rawType == Response::class.java) result.actualTypeArguments[0] else result
        }

        listOf(ApiService::class.java, MaintenanceApiService::class.java).forEach { service ->
            service.simpleName should {
                service.declaredMethods.filterNot { it.isSynthetic }.sortedBy { it.toGenericString() }.forEach { method ->
                    "convert the bodies of ${method.toGenericString().substringAfter("${service.simpleName}.")}" {
                        method.parameterAnnotations.forEachIndexed { index, annotations ->
                            if (annotations.any { it is Body }) {
                                retrofit.requestBodyConverter<Any>(method.genericParameterTypes[index], annotations, method.annotations)
                            }
                        }
                        val bodyType = responseBodyType(method)
                        if (bodyType != Void::class.java) retrofit.responseBodyConverter<Any>(bodyType, method.annotations)
                    }
                }
            }
        }
    })
