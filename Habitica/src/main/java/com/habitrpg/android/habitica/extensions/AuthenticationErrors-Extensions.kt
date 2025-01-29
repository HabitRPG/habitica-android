package com.habitrpg.android.habitica.extensions

enum class AuthenticationErrors {
    // Google Auth
    GET_CREDENTIALS_ERROR,
    INVALID_CREDENTIALS,
    // Validation
    PASSWORD_TOO_SHORT,
    PASSWORD_MISMATCH,
    MISSING_FIELDS;

    var minPasswordLength: Int = 6

    val isValidationError: Boolean
        get() = this == PASSWORD_TOO_SHORT || this == PASSWORD_MISMATCH || this == MISSING_FIELDS
}
