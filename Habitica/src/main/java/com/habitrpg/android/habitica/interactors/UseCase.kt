package com.habitrpg.android.habitica.interactors

import com.habitrpg.android.habitica.executors.PostExecutionThread
import io.reactivex.rxjava3.core.Flowable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class UseCase<Q : UseCase.RequestValues?, T: Any> protected constructor(private val postExecutionThread: PostExecutionThread) {
    protected abstract fun buildUseCaseObservable(requestValues: Q): Flowable<T>
    fun observable(requestValues: Q): Flowable<T> {
        return buildUseCaseObservable(requestValues)
            .subscribeOn(postExecutionThread.scheduler)
            .observeOn(postExecutionThread.scheduler)
    }

    interface RequestValues
}

abstract class FlowUseCase<Q : FlowUseCase.RequestValues?, T> {
    protected abstract suspend fun run(requestValues: Q): T
    suspend fun callInteractor(requestValues: Q): T {
        return withContext(Dispatchers.Main) {
            run(requestValues)
        }
    }

    interface RequestValues
}