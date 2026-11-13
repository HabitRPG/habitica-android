package com.habitrpg.android.habitica.widget.glance.state

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.glance.GlanceId
import androidx.glance.appwidget.state.updateAppWidgetState
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object WidgetStateWriter {
    private val mutex = Mutex()

    suspend fun edit(
        context: Context,
        glanceId: GlanceId,
        transform: suspend (MutablePreferences) -> Unit,
    ) = mutex.withLock {
        updateAppWidgetState(context, glanceId, transform)
    }
}
