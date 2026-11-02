package com.habitrpg.android.habitica.utils

import com.habitrpg.android.habitica.api.ServerFixtures
import com.habitrpg.shared.habitica.serialization.habiticaJson
import io.kotest.core.spec.DslDrivenSpec
import io.kotest.core.spec.style.scopes.WordSpecRootScope
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonObject

abstract class SerializerSpec(
    body: SerializerSpec.() -> Unit = {},
) : DslDrivenSpec(),
    WordSpecRootScope {
    val json = habiticaJson

    /** Parses like an API response body would be. */
    inline fun <reified T> parse(text: String): T = ServerFixtures.parse(text)

    fun <T> parse(
        serializer: KSerializer<T>,
        text: String,
    ): T = json.decodeFromString(serializer, text)

    /** Serializes like an API request body would be. */
    inline fun <reified T> serialize(value: T): JsonObject = json.parseToJsonElement(ServerFixtures.serialize(value)) as JsonObject

    init {
        body()
    }
}
