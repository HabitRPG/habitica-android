package com.habitrpg.shared.habitica.data

import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers

actual class ApiRequest<T>(private val apiCall: () -> Flowable<T>) {
    actual fun retry(): Boolean {
        return !apiCall()
                .subscribeOn(Schedulers.newThread())
                .isEmpty
                .onErrorReturnItem(true)
                .blockingGet()
    }
}