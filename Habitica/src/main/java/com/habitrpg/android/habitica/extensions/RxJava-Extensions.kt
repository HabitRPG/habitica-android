package com.habitrpg.android.habitica.extensions

import com.habitrpg.common.habitica.extensions.Optional
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

fun <S : Any, T : Optional<S>> Flowable<T>.filterOptionalDoOnEmpty(function: () -> Unit): Flowable<S> {
    return this.doOnNext { if (it.isEmpty) function() }
        .filter { !it.isEmpty }
        .map { it.assertedValue }
}

fun <S : Any, T : Optional<S>> Flowable<T>.filterMapEmpty(): Flowable<S> {
    return this.filter { !it.isEmpty }
        .map { it.assertedValue }
}

fun <S : Any, T : Any> Flowable<T>.filterMap(function: (T) -> S?): Flowable<S> {
    return this.map { Optional(function(it)) }
        .filter { !it.isEmpty }
        .map { it.assertedValue }
}

fun <S : Any, T : Optional<S>> Observable<T>.filterOptionalDoOnEmpty(function: () -> Unit): Observable<S> {
    return this.doOnNext { if (it.isEmpty) function() }
        .filter { !it.isEmpty }
        .map { it.assertedValue }
}

fun <S : Any, T : Optional<S>> Observable<T>.filterMapEmpty(): Observable<S> {
    return this.filter { !it.isEmpty }
        .map { it.assertedValue }
}

fun <S : Any, T : Any> Observable<T>.filterMap(function: (T) -> S?): Observable<S> {
    return this.map { Optional(function(it)) }
        .filter { !it.isEmpty }
        .map { it.assertedValue }
}

fun <S, T : Optional<S>> Single<T>.filterOptionalDoOnEmpty(function: () -> Unit): Maybe<S> {
    return this.doOnSuccess { if (it.isEmpty) function() }
        .filter { !it.isEmpty }
        .map { it.assertedValue }
}

fun <S, T : Optional<S>> Single<T>.filterMapEmpty(): Maybe<S> {
    return this.filter { !it.isEmpty }
        .map { it.assertedValue }
}

fun <S, T : Optional<S>> Maybe<T>.filterOptionalDoOnEmpty(function: () -> Unit): Maybe<S> {
    return this.doOnSuccess { if (it.isEmpty) function() }
        .filter { !it.isEmpty }
        .map { it.assertedValue }
}

fun <S, T : Optional<S>> Maybe<T>.filterMapEmpty(): Maybe<S> {
    return this.filter { !it.isEmpty }
        .map { it.assertedValue }
}
