package com.habitrpg.android.habitica.widget.glance.actions

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

class RefreshWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        com.habitrpg.android.habitica.widget.glance.work.WidgetRefreshWorker
            .refreshAllWidgetsNow(context)
    }
}
