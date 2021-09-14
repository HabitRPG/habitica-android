package com.habitrpg.android.habitica.extensions

import com.habitrpg.android.habitica.helpers.RxErrorHandler
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer

fun <T> Flowable<T>.subscribeWithErrorHandler(function: Consumer<T>): Disposable {
    return subscribe(function, RxErrorHandler.handleEmptyError())
}

fun <T> Flowable<T?>.skipNull(): Flowable<T> {
    @Suppress("UNCHECKED_CAST")
    return skipWhile { it == null } as? Flowable<T> ?: Flowable.empty()
}
