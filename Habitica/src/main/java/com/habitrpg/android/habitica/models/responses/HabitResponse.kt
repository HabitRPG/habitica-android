package com.habitrpg.android.habitica.models.responses

import com.habitrpg.android.habitica.models.Notification

class HabitResponse<T> {
    var data: T? = null
    var notifications: List<Notification>? = null
    var success: Boolean? = null
    var message: String? = null
}
