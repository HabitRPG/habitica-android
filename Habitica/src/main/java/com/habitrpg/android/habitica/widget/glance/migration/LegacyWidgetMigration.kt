package com.habitrpg.android.habitica.widget.glance.migration

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.preference.PreferenceManager
import com.habitrpg.android.habitica.widget.HabitButtonWidgetProvider
import com.habitrpg.android.habitica.widget.glance.data.HabitButtonWidgetCache
import com.habitrpg.android.habitica.widget.glance.data.widgetEntryPoint
import com.habitrpg.android.habitica.widget.glance.widgets.HabitButtonGlanceWidget
import kotlinx.coroutines.flow.firstOrNull

object LegacyWidgetMigration {
    private const val FLAG_PREFS = "widget_migration_flags"
    private const val FLAG_HABIT_BUTTON_V1 = "habit_button_v1_done"
    private const val LEGACY_HABIT_BUTTON_KEY_PREFIX = "habit_button_widget_"

    suspend fun runIfNeeded(context: Context) {
        val flags = context.getSharedPreferences(FLAG_PREFS, Context.MODE_PRIVATE)
        if (flags.getBoolean(FLAG_HABIT_BUTTON_V1, false)) return
        runCatching { migrateHabitButton(context) }
            .onSuccess { flags.edit { putBoolean(FLAG_HABIT_BUTTON_V1, true) } }
            .onFailure { Log.w("LegacyWidgetMigration", "habit-button migration failed", it) }
    }

    private suspend fun migrateHabitButton(context: Context) {
        val legacy = PreferenceManager.getDefaultSharedPreferences(context)
        val widgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(
            ComponentName(context, HabitButtonWidgetProvider::class.java),
        )
        if (widgetIds.isEmpty()) return

        val taskRepo = widgetEntryPoint(context).taskRepository()
        val glanceManager = GlanceAppWidgetManager(context)
        val widget = HabitButtonGlanceWidget()

        for (appWidgetId in widgetIds) {
            val legacyKey = LEGACY_HABIT_BUTTON_KEY_PREFIX + appWidgetId
            val taskId = legacy.getString(legacyKey, null) ?: continue
            val task = taskRepo.getUnmanagedTask(taskId).firstOrNull() ?: continue
            val glanceId = runCatching { glanceManager.getGlanceIdBy(appWidgetId) }
                .getOrNull() ?: continue
            HabitButtonWidgetCache.write(context, glanceId, task)
            widget.update(context, glanceId)
        }

        legacy.edit {
            for (appWidgetId in widgetIds) {
                remove(LEGACY_HABIT_BUTTON_KEY_PREFIX + appWidgetId)
            }
        }
    }
}
