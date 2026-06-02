package com.habitrpg.android.habitica.widget.glance.actions

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import com.habitrpg.android.habitica.widget.glance.data.TaskListMemoryCache
import com.habitrpg.android.habitica.widget.glance.data.widgetEntryPoint
import com.habitrpg.android.habitica.widget.glance.state.WidgetActionKeys
import com.habitrpg.android.habitica.widget.glance.state.WidgetStateKeys
import com.habitrpg.android.habitica.widget.glance.widgets.DailiesCountGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.DailyTaskListGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.TodoTaskListGlanceWidget
import com.habitrpg.shared.habitica.models.responses.TaskDirection
import com.habitrpg.shared.habitica.models.responses.TaskScoringResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class ScoreTaskAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val taskId = parameters[WidgetActionKeys.taskId] ?: return
        val direction = parameters[WidgetActionKeys.direction] ?: TaskDirection.UP.text
        val up = direction == TaskDirection.UP.text

        val manager = GlanceAppWidgetManager(context)
        val optimisticTargets = buildList {
            addAll(manager.getGlanceIds(DailyTaskListGlanceWidget::class.java))
            addAll(manager.getGlanceIds(TodoTaskListGlanceWidget::class.java))
            addAll(manager.getGlanceIds(DailiesCountGlanceWidget::class.java))
        }
        for (id in optimisticTargets) {
            updateAppWidgetState(context, id) { prefs ->
                val existing = prefs[WidgetStateKeys.taskListHiddenIds] ?: emptySet()
                prefs[WidgetStateKeys.taskListHiddenIds] = existing + taskId
            }
        }

        val entry = widgetEntryPoint(context)
        val result: TaskScoringResult? = withContext(Dispatchers.Main) {
            val user = entry.userRepository().getUser().firstOrNull()
            val res = entry.taskRepository().taskChecked(
                user = user,
                taskId = taskId,
                up = up,
                force = false,
                notifyFunc = null,
            )
            applyAvatarStatOverrides(context, user, res)
            res
        }

        TaskListMemoryCache.clear()

        showScoringToast(context, result)
        refreshStatsDependentWidgets(context)
    }
}
