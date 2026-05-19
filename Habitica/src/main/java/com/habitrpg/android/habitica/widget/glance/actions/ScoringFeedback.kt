package com.habitrpg.android.habitica.widget.glance.actions

import android.content.Context
import android.widget.Toast
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.habitrpg.android.habitica.interactors.NotifyUserUseCase
import com.habitrpg.android.habitica.widget.glance.widgets.AvatarStatsGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.DailiesCountGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.DailyTaskListGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.TodoTaskListGlanceWidget
import com.habitrpg.shared.habitica.models.responses.TaskScoringResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun showScoringToast(context: Context, result: TaskScoringResult?) {
    if (result == null) return
    val text = NotifyUserUseCase.getNotificationAndAddStatsToUserAsText(
        result.experienceDelta,
        result.healthDelta,
        result.goldDelta,
        result.manaDelta,
        result.questDamage,
    ).first
    withContext(Dispatchers.Main) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }
}

suspend fun refreshStatsDependentWidgets(context: Context) {
    val manager = GlanceAppWidgetManager(context)
    val widgets: List<GlanceAppWidget> = listOf(
        AvatarStatsGlanceWidget(),
        DailiesCountGlanceWidget(),
        DailyTaskListGlanceWidget(),
        TodoTaskListGlanceWidget(),
    )
    widgets.forEach { widget ->
        manager.getGlanceIds(widget.javaClass).forEach { id ->
            widget.update(context, id)
        }
    }
}
