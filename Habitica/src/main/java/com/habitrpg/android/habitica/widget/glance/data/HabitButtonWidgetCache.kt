package com.habitrpg.android.habitica.widget.glance.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import com.habitrpg.android.habitica.widget.glance.state.WidgetStateWriter
import com.habitrpg.android.habitica.models.tasks.Task

data class HabitButtonCachedTask(
    val taskId: String,
    val text: String,
    val value: Double,
    val up: Boolean,
    val down: Boolean,
)

object HabitButtonWidgetCache {
    val KEY_TASK_ID = stringPreferencesKey("habit_task_id")
    val KEY_TEXT = stringPreferencesKey("habit_text")
    val KEY_VALUE = doublePreferencesKey("habit_value")
    val KEY_UP = booleanPreferencesKey("habit_up")
    val KEY_DOWN = booleanPreferencesKey("habit_down")

    fun fromPrefs(prefs: Preferences): HabitButtonCachedTask? {
        val taskId = prefs[KEY_TASK_ID]?.takeIf { it.isNotEmpty() } ?: return null
        return HabitButtonCachedTask(
            taskId = taskId,
            text = prefs[KEY_TEXT] ?: "",
            value = prefs[KEY_VALUE] ?: 0.0,
            up = prefs[KEY_UP] ?: true,
            down = prefs[KEY_DOWN] ?: true,
        )
    }

    suspend fun write(context: Context, glanceId: GlanceId, task: Task) {
        val taskId = task.id ?: return
        WidgetStateWriter.edit(context, glanceId) { prefs ->
            prefs[KEY_TASK_ID] = taskId
            prefs[KEY_TEXT] = task.text
            prefs[KEY_VALUE] = task.value
            prefs[KEY_UP] = task.up == true
            prefs[KEY_DOWN] = task.down == true
        }
    }
}
