package com.habitrpg.shared.habitica.models.responses

import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorResponseTest {
    private fun error(message: String?) = HabiticaError().apply { this.message = message }

    @Test
    fun prefersFirstErrorMessage() {
        val response =
            ErrorResponse().apply {
                message = "top level"
                errors = listOf(error("first"), error("second"))
            }
        assertEquals("first", response.displayMessage)
    }

    @Test
    fun fallsBackToMessageWhenFirstErrorIsBlankOrNull() {
        assertEquals("top", ErrorResponse().apply { message = "top"; errors = listOf(error("  ")) }.displayMessage)
        assertEquals("top", ErrorResponse().apply { message = "top"; errors = listOf(error(null), error("second")) }.displayMessage)
        assertEquals("top", ErrorResponse().apply { message = "top"; errors = emptyList() }.displayMessage)
    }

    @Test
    fun emptyWhenNothingIsSet() {
        assertEquals("", ErrorResponse().displayMessage)
    }
}
