package com.habitrpg.common.habitica.models.responses

import com.habitrpg.common.habitica.models.Notification

class HabitResponse<T> {
    var data: T? = null
    var notifications: List<Notification>? = null
    var success: Boolean? = null
    var message: String? = null
}
