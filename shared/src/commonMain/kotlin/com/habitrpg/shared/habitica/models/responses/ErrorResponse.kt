package com.habitrpg.shared.habitica.models.responses

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
