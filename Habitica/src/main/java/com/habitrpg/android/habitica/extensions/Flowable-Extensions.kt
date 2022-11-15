package com.habitrpg.android.habitica.extensions

import com.habitrpg.android.habitica.helpers.ExceptionHandler
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer

fun <T : Any> Flowable<T>.subscribeWithErrorHandler(function: Consumer<T>): Disposable {
    return subscribe(function, ExceptionHandler.rx())
}
