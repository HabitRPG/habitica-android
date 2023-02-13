package com.habitrpg.android.habitica.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class UseCase<Q : UseCase.RequestValues?, T> {
    protected abstract suspend fun run(requestValues: Q): T
    suspend fun callInteractor(requestValues: Q): T {
        return withContext(Dispatchers.Main) {
            run(requestValues)
        }
    }

    interface RequestValues
}
