package com.habitrpg.android.habitica.widget.glance.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull

internal suspend fun <T> Flow<T>.firstOrNullForWidget(timeoutMillis: Long = 1_000): T? =
    withTimeoutOrNull(timeoutMillis) { firstOrNull() }
