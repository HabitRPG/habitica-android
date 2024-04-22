package com.habitrpg.wearos.habitica.models

sealed class NetworkResult<out T : Any> {
    val isResponseFresh: Boolean
        get() =
            when (this) {
                is Success -> this.isFresh
                is Error -> this.isFresh
            }
    val responseData: T?
        get() {
            return if (this is Success) {
                this.data
            } else {
                null
            }
        }

    val isSuccess: Boolean
        get() = this is Success
    val isError: Boolean
        get() = this is Error

    data class Success<out T : Any>(val data: T, internal val isFresh: Boolean) : NetworkResult<T>()

    data class Error(val exception: Exception, internal val isFresh: Boolean) : NetworkResult<Nothing>()
}
