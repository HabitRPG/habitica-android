package com.habitrpg.wearos.habitica.models

import kotlinx.serialization.Serializable

@Serializable
class WearableHabitResponse<T> {
    var data: T? = null
    var success: Boolean = false
    var message: String? = null
    val isFresh: Boolean = true
}
