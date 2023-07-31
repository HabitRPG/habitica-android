package com.habitrpg.android.habitica.extensions

import com.google.gson.JsonObject

fun JsonObject?.getAsString(key: String): String {
    if (this?.get(key)?.isJsonPrimitive == true) {
        return this.get(key)?.asString ?: ""
    }
    return ""
}
