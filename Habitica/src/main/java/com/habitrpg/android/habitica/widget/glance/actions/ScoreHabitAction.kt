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
import com.habitrpg.shared.habitica.models.responses.TaskScoringResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class ScoreHabitAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val taskId = parameters[WidgetActionKeys.taskId] ?: return
        val direction = parameters[WidgetActionKeys.direction] ?: TaskDirection.UP.text
        val up = direction == TaskDirection.UP.text

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
            runCatching {
                val task = entry.taskRepository().getTask(taskId).firstOrNull()
                if (task != null) {
                    HabitButtonWidgetCache.write(context, glanceId, task)
                }
            }
            applyAvatarStatOverrides(context, user, res)
            res
        }

        showScoringToast(context, result)

        HabitButtonGlanceWidget().update(context, glanceId)
        refreshStatsDependentWidgets(context)
    }
}
