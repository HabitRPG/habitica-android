package com.habitrpg.shared.habitica.data.implementation

import com.habitrpg.shared.habitica.data.ApiRequest
import com.habitrpg.shared.habitica.data.OfflineClient

class OfflineClientImpl : OfflineClient {
    private var pendingRequests: MutableList<ApiRequest<*>> = mutableListOf()

    /**
     * Add the request to the queue
     */
    override fun addPendingRequest(request: ApiRequest<*>) {
        pendingRequests.add(request)
    }

    /**
     * Should attempt to clear the queue returning the bulk request response
     */
    override fun trySubmitPendingRequests() {
        val failedRequests: MutableList<ApiRequest<*>> = mutableListOf()
        for (pendingRequest in pendingRequests) {
            if (!pendingRequest.retry()) {
                failedRequests.add(pendingRequest)
            }
        }
        pendingRequests = failedRequests
    }

    override fun hasPendingRequest(): Boolean {
        return pendingRequests.isNotEmpty()
    }
}

