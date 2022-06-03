package com.habitrpg.android.habitica.models.responses

import com.habitrpg.common.habitica.models.responses.HabiticaError

class ErrorResponse {
    var message: String? = null
    var errors: List<HabiticaError>? = null
    val displayMessage: String
        get() {
            if (errors?.isNotEmpty() == true) {
                val error = errors?.get(0)
                if (error?.message?.isNotBlank() == true) {
                    return error.message ?: ""
                }
            }
            return message ?: ""
        }
}
