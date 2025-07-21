package com.habitrpg.android.habitica.extensions

enum class AuthenticationErrors {
    // Google Auth
    GET_CREDENTIALS_ERROR,
    INVALID_CREDENTIALS,
    INVALID_CREDENTIAL_TYPE,
    UNKNOWN_CREDENTIAL_TYPE,
    MISSING_TOKEN,
    // Validation
    INVALID_EMAIL,
    PASSWORD_TOO_SHORT,
    PASSWORD_MISMATCH,
    MISSING_FIELDS;

    var minPasswordLength: Int = 6

    val isValidationError: Boolean
        get() = this == PASSWORD_TOO_SHORT || this == PASSWORD_MISMATCH || this == MISSING_FIELDS

    var message: String? = null
}
