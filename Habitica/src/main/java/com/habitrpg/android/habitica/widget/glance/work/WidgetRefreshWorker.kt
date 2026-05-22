package com.habitrpg.android.habitica.widget.glance.work

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.habitrpg.android.habitica.widget.AvatarWidgetProvider
import androidx.glance.appwidget.state.updateAppWidgetState
import com.habitrpg.android.habitica.widget.glance.data.AvatarBitmapCache
import com.habitrpg.android.habitica.widget.glance.data.TaskListMemoryCache
import com.habitrpg.android.habitica.widget.glance.data.widgetEntryPoint
import com.habitrpg.android.habitica.widget.glance.state.WidgetStateKeys
import com.habitrpg.android.habitica.widget.glance.widgets.AddTaskMultiGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.AddTaskSingleGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.AvatarStatsGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.DailiesCountGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.DailyTaskListGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.HabitButtonGlanceWidget
import com.habitrpg.android.habitica.widget.glance.widgets.TodoTaskListGlanceWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class WidgetRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val user = widgetEntryPoint(context).userRepository().getUser().firstOrNull()
        if (user == null) return Result.success()
        withContext(Dispatchers.Main) {
            AvatarBitmapCache.refreshIfNeeded(context, user)
        }
        TaskListMemoryCache.clear()
        refreshAllWidgets(context)
        AvatarWidgetProvider.renderAll(context)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "habitica_widget_refresh"
        private val REFRESH_INTERVAL_MINUTES = 15L

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

        suspend fun refreshAllWidgetsNow(context: Context) {
            withContext(Dispatchers.Main) {
                val user = widgetEntryPoint(context).userRepository().getUser().firstOrNull()
                AvatarBitmapCache.refreshIfNeeded(context, user)
            }
            TaskListMemoryCache.clear()
            refreshAllWidgets(context)
            AvatarWidgetProvider.renderAll(context)
        }

        private suspend fun refreshAllWidgets(context: Context) {
            val manager = GlanceAppWidgetManager(context)
            val hiddenIdsClasses = listOf(
                DailyTaskListGlanceWidget::class.java,
                TodoTaskListGlanceWidget::class.java,
                DailiesCountGlanceWidget::class.java,
            )
            hiddenIdsClasses.forEach { cls ->
                manager.getGlanceIds(cls).forEach { id ->
                    updateAppWidgetState(context, id) { prefs ->
                        prefs.remove(WidgetStateKeys.taskListHiddenIds)
                    }
                }
            }
            manager.getGlanceIds(AvatarStatsGlanceWidget::class.java).forEach { id ->
                updateAppWidgetState(context, id) { prefs ->
                    prefs.remove(WidgetStateKeys.statOverrideValid)
                    prefs.remove(WidgetStateKeys.statOverrideHp)
                    prefs.remove(WidgetStateKeys.statOverrideExp)
                    prefs.remove(WidgetStateKeys.statOverrideMp)
                    prefs.remove(WidgetStateKeys.statOverrideGold)
                }
            }
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
