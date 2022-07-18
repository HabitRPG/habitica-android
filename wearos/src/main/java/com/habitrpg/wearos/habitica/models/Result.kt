package com.habitrpg.wearos.habitica.models

sealed class NetworkResult<out T : Any> {
    val isResponseFresh: Boolean
    get() = if (this is Success) {
        this.isFresh
    } else if (this is Error) {
        this.isFresh
    } else {
        false
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

    data class Success<out T : Any>(val data: T, val isFresh: Boolean) : NetworkResult<T>()
    data class Error(val exception: Exception, val isFresh: Boolean) : NetworkResult<Nothing>()
}