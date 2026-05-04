package com.habitrpg.android.habitica.widget.glance.work

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.habitrpg.android.habitica.widget.glance.data.AvatarBitmapCache
import com.habitrpg.android.habitica.widget.glance.data.widgetEntryPoint
import com.habitrpg.android.habitica.widget.glance.widgets.AddTaskMultiGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.AddTaskSingleGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.AvatarStatsGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.DailiesCountGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.DailyTaskListGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.HabitButtonGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.TodoTaskListGlanceWidget
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit

class WidgetRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val user = widgetEntryPoint(context).userRepository().getUser().firstOrNull()
        AvatarBitmapCache.refreshIfNeeded(context, user)
        refreshAllWidgets(context)
        AvatarWidgetRenderer.pushUpdate(context)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "habitica_widget_refresh"
        private val REFRESH_INTERVAL_MINUTES = 15L

        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(
                REFRESH_INTERVAL_MINUTES, TimeUnit.MINUTES,
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }

        suspend fun refreshAllWidgetsNow(context: Context) {
            val user = widgetEntryPoint(context).userRepository().getUser().firstOrNull()
            AvatarBitmapCache.refreshIfNeeded(context, user)
            refreshAllWidgets(context)
            AvatarWidgetRenderer.pushUpdate(context)
        }

        private suspend fun refreshAllWidgets(context: Context) {
            val manager = GlanceAppWidgetManager(context)
            val widgets: List<GlanceAppWidget> = listOf(
                AvatarStatsGlanceWidget(),
                DailyTaskListGlanceWidget(),
                TodoTaskListGlanceWidget(),
                DailiesCountGlanceWidget(),
                AddTaskSingleGlanceWidget(),
                AddTaskMultiGlanceWidget(),
                HabitButtonGlanceWidget(),
            )
            widgets.forEach { widget ->
                manager.getGlanceIds(widget.javaClass).forEach { id ->
                    widget.update(context, id)
                }
            }
        }
    }
}
