package com.habitrpg.android.habitica.extensions

import com.habitrpg.android.habitica.helpers.RxErrorHandler
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer

fun <T> Flowable<T>.subscribeWithErrorHandler(function: Consumer<T>): Disposable {
    return subscribe(function, RxErrorHandler.handleEmptyError())
}