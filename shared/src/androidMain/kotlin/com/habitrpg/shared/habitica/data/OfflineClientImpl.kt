package com.habitrpg.shared.habitica.data

import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import java.net.SocketException
import javax.net.ssl.SSLException

actual class ApiRequest<T>(private val apiCall: () -> Flowable<T>) {
    actual fun retry(): Boolean {
        return !apiCall()
                .subscribeOn(Schedulers.newThread())
                .isEmpty
                .onErrorReturn { throwable ->
                    !(throwable is SocketException || throwable is SSLException)
                }
                .blockingGet()
    }
}