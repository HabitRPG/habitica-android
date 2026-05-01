package com.habitrpg.android.habitica.widget.glance.actions

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import com.habitrpg.android.habitica.widget.glance.data.widgetEntryPoint
import com.habitrpg.android.habitica.widget.glance.widgets.AvatarStatsGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.DailiesCountGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.DailyTaskListGlanceWidget

class RunCronAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        widgetEntryPoint(context).userRepository().runCron()
        val manager = GlanceAppWidgetManager(context)
        listOf(
            DailiesCountGlanceWidget(),
            DailyTaskListGlanceWidget(),
            AvatarStatsGlanceWidget(),
        ).forEach { widget -> refreshWidget(manager, context, widget) }
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
