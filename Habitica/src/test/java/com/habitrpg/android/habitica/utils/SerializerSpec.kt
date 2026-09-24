package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.habitrpg.android.habitica.api.GSonFactoryCreator
import io.kotest.core.spec.DslDrivenSpec
import io.kotest.core.spec.style.scopes.WordSpecRootScope
import io.mockk.mockk
import java.lang.reflect.Type

abstract class SerializerSpec(
    body: SerializerSpec.() -> Unit = {},
) : DslDrivenSpec(),
    WordSpecRootScope {
    val deserializationContext: JsonDeserializationContext = mockk(relaxed = true)
    val serializationContext: JsonSerializationContext = mockk(relaxed = true)
    val gson = GSonFactoryCreator.createGson()
    val gsonDeserializationContext =
        object : JsonDeserializationContext {
            override fun <T> deserialize(
                json: JsonElement,
                typeOfT: Type,
            ): T = gson.fromJson(json, typeOfT)
        }

    init {
        body()
    }
}
