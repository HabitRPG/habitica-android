package com.habitrpg.android.habitica.widget.glance.actions

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.habitrpg.android.habitica.widget.glance.data.HabitButtonWidgetCache
import com.habitrpg.android.habitica.widget.glance.data.widgetEntryPoint
import com.habitrpg.android.habitica.widget.glance.state.WidgetActionKeys
import com.habitrpg.android.habitica.widget.glance.widgets.HabitButtonGlanceWidget
import com.habitrpg.shared.habitica.models.responses.TaskDirection
import kotlinx.coroutines.flow.firstOrNull

class ScoreHabitAction : ActionCallback {
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

        runCatching {
            val task = entry.taskRepository().getTask(taskId).firstOrNull()
            if (task != null) {
                HabitButtonWidgetCache.write(context, glanceId, task)
            }
        }

        HabitButtonGlanceWidget().update(context, glanceId)
    }
}
