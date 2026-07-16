package com.habitrpg.android.habitica.widget.glance.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.habitrpg.android.habitica.widget.glance.state.WidgetStateWriter
import kotlinx.coroutines.withTimeoutOrNull

private const val HYDRATION_TIMEOUT_MS = 5_000L

suspend fun hydrateSnapshot(
    context: Context,
    id: GlanceId,
    key: Preferences.Key<String>,
    load: suspend () -> String?,
): String? {
    getAppWidgetState(context, PreferencesGlanceStateDefinition, id)[key]?.let { return it }

    val json = withTimeoutOrNull(HYDRATION_TIMEOUT_MS) { load() } ?: return null

    var effective = json
    WidgetStateWriter.edit(context, id) { prefs ->
        val existing = prefs[key]
        if (existing == null) prefs[key] = json else effective = existing
    }
    return effective
}
