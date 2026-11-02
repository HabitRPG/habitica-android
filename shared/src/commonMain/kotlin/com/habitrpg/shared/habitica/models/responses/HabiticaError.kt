package com.habitrpg.shared.habitica.models.responses

import kotlinx.serialization.Serializable

@Serializable
class HabiticaError {
    var message: String? = null
    var value: String? = null
}
