package com.habitrpg.android.habitica.widget.glance.actions

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.habitrpg.android.habitica.widget.glance.data.widgetEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RunCronAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        withContext(Dispatchers.Main) {
            widgetEntryPoint(context).userRepository().runCron()
        }
        refreshStatsDependentWidgets(context)
    }
}
