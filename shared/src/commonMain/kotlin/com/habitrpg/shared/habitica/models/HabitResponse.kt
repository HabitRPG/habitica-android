package com.habitrpg.shared.habitica.models

import kotlinx.serialization.Serializable

@Serializable
class HabitResponse<T> {
    var data: T? = null
    var notifications: List<Notification>? = null
    var success: Boolean? = null
    var message: String? = null
    var statusCode: Int? = null

    // Version of the server's client config, changes whenever the config does
    var clientConfigV: String? = null
}
