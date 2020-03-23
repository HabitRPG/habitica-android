package com.habitrpg.shared.habitica.data

expect class ApiRequest<T> {
    fun retry(): Boolean
}

interface OfflineClient {
    fun trySubmitPendingRequests()
    fun addPendingRequest(request: ApiRequest<*>)

    /**
     * Check the state of the client to see if it is offline
     *
     * @return Boolean isOffline
     */
    fun hasPendingRequest(): Boolean
}

