package com.habitrpg.android.habitica.extensions

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single


fun <S, T : Optional<S>> Flowable<T>.filterOptionalDoOnEmpty(function: () -> Unit): Flowable<S> {
    return this.doOnNext { if (it.isEmpty) function() }
            .filter { !it.isEmpty }
            .map { it.value }
}

fun <S, T : Optional<S>> Flowable<T>.filterMapEmpty(): Flowable<S> {
    return this.filter { !it.isEmpty }
            .map { it.value }
}

fun <S, T : Optional<S>> Observable<T>.filterOptionalDoOnEmpty(function: () -> Unit): Observable<S> {
    return this.doOnNext { if (it.isEmpty) function() }
            .filter { !it.isEmpty }
            .map { it.value }
}

fun <S, T : Optional<S>> Observable<T>.filterMapEmpty(): Observable<S> {
    return this.filter { !it.isEmpty }
            .map { it.value }
}

fun <S, T : Optional<S>> Single<T>.filterOptionalDoOnEmpty(function: () -> Unit): Maybe<S> {
    return this.doAfterSuccess { if (it.isEmpty) function() }
            .filter { !it.isEmpty }
            .map { it.value }
}

fun <S, T : Optional<S>> Single<T>.filterMapEmpty(): Maybe<S> {
    return this.filter { !it.isEmpty }
            .map { it.value }
}

fun <S, T : Optional<S>> Maybe<T>.filterOptionalDoOnEmpty(function: () -> Unit): Maybe<S> {
    return this.doAfterSuccess { if (it.isEmpty) function() }
            .filter { !it.isEmpty }
            .map { it.value }
}

fun <S, T : Optional<S>> Maybe<T>.filterMapEmpty(): Maybe<S> {
    return this.filter { !it.isEmpty }
            .map { it.value }
}