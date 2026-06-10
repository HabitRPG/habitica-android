package com.habitrpg.android.habitica.widget.glance.work

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.habitrpg.android.habitica.widget.glance.data.AvatarBitmapCache
import com.habitrpg.android.habitica.widget.glance.data.WidgetAuth
import com.habitrpg.android.habitica.widget.glance.state.WidgetStateWriter
import com.habitrpg.android.habitica.widget.glance.widgets.AddTaskMultiGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.AddTaskSingleGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.AvatarStatsGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.DailiesCountGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.DailyTaskListGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.HabitButtonGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.TodoTaskListGlanceWidget
import java.util.concurrent.TimeUnit

class WidgetRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        if (!WidgetAuth.isLoggedIn(context)) return Result.success()
        WidgetSnapshotPublisher.publishAll(context)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "habitica_widget_refresh"
        private const val REFRESH_INTERVAL_MINUTES = 15L

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
            val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(
                REFRESH_INTERVAL_MINUTES, TimeUnit.MINUTES,
            ).setConstraints(constraints).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }

        fun enqueueOneTime(context: Context) {
            val request = OneTimeWorkRequestBuilder<WidgetRefreshWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }

        suspend fun refreshAllWidgetsNow(context: Context, reconcileHiddenIds: Boolean = false) {
            WidgetSnapshotPublisher.publishAll(context)
        }

        suspend fun refreshTaskListWidgetsNow(context: Context) {
            WidgetSnapshotPublisher.publishTaskLists(context)
            WidgetSnapshotPublisher.publishDailyCount(context)
        }

        suspend fun clearAllForLogout(context: Context) {
            AvatarBitmapCache.clearCache(context)
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
                    WidgetStateWriter.edit(context, id) { prefs -> prefs.clear() }
                    widget.update(context, id)
                }
            }
        }
    }
}
