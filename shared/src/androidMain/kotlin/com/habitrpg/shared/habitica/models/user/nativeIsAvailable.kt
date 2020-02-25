package com.habitrpg.shared.habitica.models.user

import android.text.TextUtils

actual fun nativeIsAvailable(outfit: String): Boolean {
    return !TextUtils.isEmpty(outfit) && !outfit.endsWith("base_0")
}