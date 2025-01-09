package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonSerializationContext
import io.kotest.core.spec.DslDrivenSpec
import io.kotest.core.spec.style.scopes.WordSpecRootScope
import io.mockk.mockk

abstract class SerializerSpec(body: SerializerSpec.() -> Unit = {}) :
    DslDrivenSpec(),
    WordSpecRootScope {
    val deserializationContext: JsonDeserializationContext = mockk(relaxed = true)
    val serializationContext: JsonSerializationContext = mockk(relaxed = true)

    init {
        body()
    }
}
