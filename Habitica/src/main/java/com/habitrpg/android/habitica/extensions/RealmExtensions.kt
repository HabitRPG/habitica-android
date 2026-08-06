package com.habitrpg.android.habitica.extensions

import io.realm.RealmModel
import io.realm.RealmQuery
import io.realm.kotlin.isValid
import io.realm.kotlin.toFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull

fun <T: RealmModel> RealmQuery<T>.findAllAsFlow(): Flow<List<T>> = this.findAll()
    .toFlow()
    .filter { it.isLoaded }

fun <T: RealmModel> RealmQuery<T>.findOneAsFlow(): Flow<T> = this.findAll()
    .toFlow()
    .filter { it.isLoaded && it.isNotEmpty() }
    .mapNotNull { it.first() }
    .filter { it.isValid() }
