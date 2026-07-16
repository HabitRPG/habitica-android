package com.habitrpg.android.habitica.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.habitrpg.android.habitica.widget.glance.work.WidgetRefreshWorker

abstract class HydratingGlanceReceiver : GlanceAppWidgetReceiver() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        WidgetRefreshWorker.enqueueHydration(context)
    }
}
