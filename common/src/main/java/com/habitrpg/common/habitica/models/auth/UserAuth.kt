package com.habitrpg.common.habitica.models.auth

data class UserAuth(
    val username: String="",
    val password: String="",
    val confirmPassword: String="",
    val email: String=""
)
