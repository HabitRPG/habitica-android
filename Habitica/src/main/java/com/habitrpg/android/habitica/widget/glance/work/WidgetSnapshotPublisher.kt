package com.habitrpg.android.habitica.widget.glance.work

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.habitrpg.android.habitica.widget.AvatarWidgetProvider
import com.habitrpg.android.habitica.widget.glance.data.HabitButtonWidgetCache
import com.habitrpg.android.habitica.widget.glance.data.WidgetSnapshotStore
import com.habitrpg.android.habitica.widget.glance.data.loadDailyCountStateOrNull
import com.habitrpg.android.habitica.widget.glance.data.loadStatsStateOrNull
import com.habitrpg.android.habitica.widget.glance.data.loadTaskListStateOrNull
import com.habitrpg.android.habitica.widget.glance.data.widgetEntryPoint
import com.habitrpg.android.habitica.widget.glance.state.WidgetStateWriter
import com.habitrpg.android.habitica.widget.glance.widgets.AvatarStatsGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.DailiesCountGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.DailyTaskListGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.HabitButtonGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.TodoTaskListGlanceWidget
import com.habitrpg.shared.habitica.models.tasks.TaskType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

object WidgetSnapshotPublisher {

    suspend fun publishAll(context: Context) {
        publishStats(context)
        publishTaskLists(context)
        publishDailyCount(context)
        publishHabitButtons(context)
        AvatarWidgetProvider.renderAll(context)
    }

    suspend fun publishHabitButtons(context: Context) {
        val widget = HabitButtonGlanceWidget()
        val manager = GlanceAppWidgetManager(context)
        val entry = widgetEntryPoint(context)
        manager.getGlanceIds(widget.javaClass).forEach { id ->
            val taskId = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)[HabitButtonWidgetCache.KEY_TASK_ID]
                ?.takeIf { it.isNotEmpty() } ?: return@forEach
            val task = withContext(Dispatchers.Main) {
                entry.taskRepository().getTask(taskId).firstOrNull()
            } ?: return@forEach
            val up = task.up == true
            val down = task.down == true
            var changed = false
            WidgetStateWriter.edit(context, id) { prefs ->
                if (prefs[HabitButtonWidgetCache.KEY_TEXT] != task.text ||
                    prefs[HabitButtonWidgetCache.KEY_VALUE] != task.value ||
                    prefs[HabitButtonWidgetCache.KEY_UP] != up ||
                    prefs[HabitButtonWidgetCache.KEY_DOWN] != down
                ) {
                    prefs[HabitButtonWidgetCache.KEY_TASK_ID] = task.id ?: taskId
                    prefs[HabitButtonWidgetCache.KEY_TEXT] = task.text
                    prefs[HabitButtonWidgetCache.KEY_VALUE] = task.value
                    prefs[HabitButtonWidgetCache.KEY_UP] = up
                    prefs[HabitButtonWidgetCache.KEY_DOWN] = down
                    changed = true
                }
            }
            if (changed) widget.update(context, id)
        }
    }

    suspend fun publishStats(context: Context) {
        val state = loadStatsStateOrNull(context) ?: return
        commit(
            context,
            AvatarStatsGlanceWidget(),
            WidgetSnapshotStore.statsKey,
            WidgetSnapshotStore.encodeStats(state),
        )
    }

    suspend fun publishTaskLists(context: Context) {
        loadTaskListStateOrNull(context, TaskType.DAILY)?.let {
            commit(
                context,
                DailyTaskListGlanceWidget(),
                WidgetSnapshotStore.taskListKey,
                WidgetSnapshotStore.encodeTaskList(it),
            )
        }
        loadTaskListStateOrNull(context, TaskType.TODO)?.let {
            commit(
                context,
                TodoTaskListGlanceWidget(),
                WidgetSnapshotStore.taskListKey,
                WidgetSnapshotStore.encodeTaskList(it),
            )
        }
    }

    suspend fun publishDailyCount(context: Context) {
        val state = loadDailyCountStateOrNull(context) ?: return
        commit(
            context,
            DailiesCountGlanceWidget(),
            WidgetSnapshotStore.dailyCountKey,
            WidgetSnapshotStore.encodeDailyCount(state),
        )
    }

    suspend fun optimisticComplete(context: Context, taskId: String) {
        val wasDaily = removeFromList(context, DailyTaskListGlanceWidget(), taskId)
        removeFromList(context, TodoTaskListGlanceWidget(), taskId)
        if (wasDaily) bumpDailyCount(context)
    }

    private suspend fun commit(
        context: Context,
        widget: GlanceAppWidget,
        key: Preferences.Key<String>,
        json: String,
    ) {
        val manager = GlanceAppWidgetManager(context)
        manager.getGlanceIds(widget.javaClass).forEach { id ->
            var changed = false
            WidgetStateWriter.edit(context, id) { prefs ->
                if (prefs[key] != json) {
                    prefs[key] = json
                    changed = true
                }
            }
            if (changed) widget.update(context, id)
        }
    }

    private suspend fun removeFromList(
        context: Context,
        widget: GlanceAppWidget,
        taskId: String,
    ): Boolean {
        val manager = GlanceAppWidgetManager(context)
        var removedAny = false
        manager.getGlanceIds(widget.javaClass).forEach { id ->
            var changed = false
            WidgetStateWriter.edit(context, id) { prefs ->
                val current = WidgetSnapshotStore.taskListFrom(prefs) ?: return@edit
                if (current.tasks.any { it.id == taskId }) {
                    val next = current.copy(tasks = current.tasks.filterNot { it.id == taskId })
                    prefs[WidgetSnapshotStore.taskListKey] = WidgetSnapshotStore.encodeTaskList(next)
                    changed = true
                }
            }
            if (changed) {
                removedAny = true
                widget.update(context, id)
            }
        }
        return removedAny
    }

    private suspend fun bumpDailyCount(context: Context) {
        val widget = DailiesCountGlanceWidget()
        val manager = GlanceAppWidgetManager(context)
        manager.getGlanceIds(widget.javaClass).forEach { id ->
            var changed = false
            WidgetStateWriter.edit(context, id) { prefs ->
                val current = WidgetSnapshotStore.dailyCountFrom(prefs) ?: return@edit
                val nextCompleted = (current.completed + 1).coerceAtMost(current.totalDue)
                if (nextCompleted != current.completed) {
                    prefs[WidgetSnapshotStore.dailyCountKey] =
                        WidgetSnapshotStore.encodeDailyCount(current.copy(completed = nextCompleted))
                    changed = true
                }
            }
            if (changed) widget.update(context, id)
        }
    }
}
