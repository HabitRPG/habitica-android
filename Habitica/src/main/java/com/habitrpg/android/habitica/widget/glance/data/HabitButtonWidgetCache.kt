package com.habitrpg.android.habitica.widget.glance.data

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.habitrpg.android.habitica.models.tasks.Task

data class HabitButtonCachedTask(
    val taskId: String,
    val text: String,
    val value: Double,
    val up: Boolean,
    val down: Boolean,
)

object HabitButtonWidgetCache {
    private fun base(widgetId: Int) = "habit_button_widget_$widgetId"
    private fun keyText(widgetId: Int) = "${base(widgetId)}_text"
    private fun keyValue(widgetId: Int) = "${base(widgetId)}_value"
    private fun keyUp(widgetId: Int) = "${base(widgetId)}_up"
    private fun keyDown(widgetId: Int) = "${base(widgetId)}_down"
    private fun keyHasCache(widgetId: Int) = "${base(widgetId)}_cached"

    fun read(context: Context, widgetId: Int): HabitButtonCachedTask? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val taskId = prefs.getString(base(widgetId), null)?.takeIf { it.isNotEmpty() }
            ?: return null
        if (!prefs.getBoolean(keyHasCache(widgetId), false)) return null
        val text = prefs.getString(keyText(widgetId), "") ?: ""
        val valueFloat = prefs.getFloat(keyValue(widgetId), 0f)
        val up = prefs.getBoolean(keyUp(widgetId), true)
        val down = prefs.getBoolean(keyDown(widgetId), true)
        return HabitButtonCachedTask(taskId, text, valueFloat.toDouble(), up, down)
    }

    fun write(context: Context, widgetId: Int, task: Task) {
        val taskId = task.id ?: return
        PreferenceManager.getDefaultSharedPreferences(context).edit(commit = true) {
            putString(base(widgetId), taskId)
            putString(keyText(widgetId), task.text)
            putFloat(keyValue(widgetId), task.value.toFloat())
            putBoolean(keyUp(widgetId), task.up == true)
            putBoolean(keyDown(widgetId), task.down == true)
            putBoolean(keyHasCache(widgetId), true)
        }
    }
}
