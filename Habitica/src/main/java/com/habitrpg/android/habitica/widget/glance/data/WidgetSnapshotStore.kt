package com.habitrpg.android.habitica.widget.glance.data

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.habitrpg.shared.habitica.serialization.habiticaJson
import kotlinx.serialization.encodeToString

object WidgetSnapshotStore {
    val statsKey = stringPreferencesKey("stats_snapshot")
    val taskListKey = stringPreferencesKey("task_list_snapshot")
    val dailyCountKey = stringPreferencesKey("daily_count_snapshot")
    val configKey = stringPreferencesKey("render_config")

    fun fontScale(context: android.content.Context): Float =
        runCatching {
            android.provider.Settings.System.getFloat(
                context.contentResolver,
                android.provider.Settings.System.FONT_SCALE,
            )
        }.getOrNull() ?: context.resources.configuration.fontScale

    fun configFingerprint(context: android.content.Context): String {
        val config = context.resources.configuration
        return "${fontScale(context)}|${config.densityDpi}|${config.locales.toLanguageTags()}"
    }

    fun encodeStats(state: StatsWidgetState): String = habiticaJson.encodeToString(state)

    fun encodeTaskList(state: TaskListWidgetState): String = habiticaJson.encodeToString(state)

    fun encodeDailyCount(state: DailyCountWidgetState): String = habiticaJson.encodeToString(state)

    fun statsFrom(prefs: Preferences): StatsWidgetState? = decode<StatsWidgetState>(prefs[statsKey])

    fun taskListFrom(prefs: Preferences): TaskListWidgetState? = decode<TaskListWidgetState>(prefs[taskListKey])

    fun dailyCountFrom(prefs: Preferences): DailyCountWidgetState? = decode<DailyCountWidgetState>(prefs[dailyCountKey])

    fun decodeStats(json: String): StatsWidgetState? = decode<StatsWidgetState>(json)

    fun decodeTaskList(json: String): TaskListWidgetState? = decode<TaskListWidgetState>(json)

    fun decodeDailyCount(json: String): DailyCountWidgetState? = decode<DailyCountWidgetState>(json)

    private inline fun <reified T> decode(json: String?): T? =
        json?.let { runCatching { habiticaJson.decodeFromString<T>(it) }.getOrNull() }
}
