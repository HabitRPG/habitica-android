package com.habitrpg.android.habitica.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import com.habitrpg.android.habitica.widget.glance.widgets.DailiesCountGlanceWidget
import com.habitrpg.android.habitica.widget.glance.work.CronBoundaryRefreshWorker

class DailiesCountWidgetReceiver : HydratingGlanceReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailiesCountGlanceWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        CronBoundaryRefreshWorker.scheduleFromCacheAsync(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        CronBoundaryRefreshWorker.scheduleFromCacheAsync(context)
    }
}
