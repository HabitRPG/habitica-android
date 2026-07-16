package com.habitrpg.android.habitica.widget.glance.data

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson

object WidgetSnapshotStore {
    private val gson = Gson()

    val statsKey = stringPreferencesKey("stats_snapshot")
    val taskListKey = stringPreferencesKey("task_list_snapshot")
    val dailyCountKey = stringPreferencesKey("daily_count_snapshot")

    fun encodeStats(state: StatsWidgetState): String = gson.toJson(state)

    fun encodeTaskList(state: TaskListWidgetState): String = gson.toJson(state)

    fun encodeDailyCount(state: DailyCountWidgetState): String = gson.toJson(state)

    fun statsFrom(prefs: Preferences): StatsWidgetState? = decode(prefs[statsKey], StatsWidgetState::class.java)

    fun taskListFrom(prefs: Preferences): TaskListWidgetState? = decode(prefs[taskListKey], TaskListWidgetState::class.java)

    fun dailyCountFrom(prefs: Preferences): DailyCountWidgetState? = decode(prefs[dailyCountKey], DailyCountWidgetState::class.java)

    fun decodeStats(json: String): StatsWidgetState? = decode(json, StatsWidgetState::class.java)

    fun decodeTaskList(json: String): TaskListWidgetState? = decode(json, TaskListWidgetState::class.java)

    fun decodeDailyCount(json: String): DailyCountWidgetState? = decode(json, DailyCountWidgetState::class.java)

    private fun <T> decode(json: String?, type: Class<T>): T? =
        json?.let { runCatching { gson.fromJson(it, type) }.getOrNull() }
}
