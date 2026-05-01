package com.habitrpg.android.habitica.widget.glance.actions

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import com.habitrpg.android.habitica.widget.glance.data.widgetEntryPoint
import com.habitrpg.android.habitica.widget.glance.state.WidgetActionKeys
import com.habitrpg.android.habitica.widget.glance.widgets.DailyTaskListGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.TodoTaskListGlanceWidget
import com.habitrpg.shared.habitica.models.responses.TaskDirection
import kotlinx.coroutines.flow.firstOrNull

class ScoreTaskAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val taskId = parameters[WidgetActionKeys.taskId] ?: return
        val direction = parameters[WidgetActionKeys.direction] ?: TaskDirection.UP.text

        val entry = widgetEntryPoint(context)
        val user = entry.userRepository().getUser().firstOrNull()
        entry.taskRepository().taskChecked(
            user = user,
            taskId = taskId,
            up = direction == TaskDirection.UP.text,
            force = false,
            notifyFunc = null,
        )

        refreshTaskListWidgets(context)
    }

    private suspend fun refreshTaskListWidgets(context: Context) {
        val manager = GlanceAppWidgetManager(context)
        refreshWidget(manager, context, DailyTaskListGlanceWidget())
        refreshWidget(manager, context, TodoTaskListGlanceWidget())
    }

    private suspend fun refreshWidget(
        manager: GlanceAppWidgetManager,
        context: Context,
        widget: GlanceAppWidget,
    ) {
        manager.getGlanceIds(widget.javaClass).forEach { id ->
            widget.update(context, id)
        }
    }
}
