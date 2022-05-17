package com.habitrpg.common.habitica.extensions

data class Optional<T>(val value: T?) {
    val isEmpty = value == null

    val assertedValue: T
        get() {
            assert(!isEmpty)
            return value!!
        }
}
fun <T> T?.asOptional() = Optional(this)
